package org.transmartproject.search.indexing.modules

import com.google.common.collect.BiMap
import com.google.common.collect.ImmutableBiMap
import com.google.common.collect.ImmutableSet
import groovy.util.logging.Log4j
import org.hibernate.SQLQuery
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.transmartproject.search.indexing.*

import static org.transmartproject.search.indexing.FacetsIndexingService.FIELD_CONCEPT_PATH
import static org.transmartproject.search.indexing.FacetsIndexingService.FIELD_FOLDER_ID
import static org.transmartproject.search.indexing.modules.AbstractFacetsIndexingFolderModule.FOLDER_DOC_TYPE

@Log4j
@Component
class ManagedConceptTagsIndexingModule implements FacetsIndexingModule {

    @Autowired
    SessionFactory sessionFactory

    final String name = 'managed_concept_tags'

    final Set<String> supportedDocumentTypes = ImmutableSet.of(FOLDER_DOC_TYPE)

    private final static BiMap<String, FacetsFieldType> DB_FIELD_TYPE_MAPPING =
            ImmutableBiMap.of('ANALYZED_STRING',     FacetsFieldType.TEXT,
                            'NON_ANALYZED_STRING', FacetsFieldType.STRING,
                            'DATE',                FacetsFieldType.DATE,
                            'INTEGER',             FacetsFieldType.INTEGER,
                            'FLOAT',               FacetsFieldType.FLOAT,)

    public static final int MANAGED_TAGS_FIELDS_BASE_PRECEDENCE =
            FacetsFieldDisplaySettings.BROWSE_FIELDS_DEFAULT_PRECEDENCE + 10

    @Override
    Iterator<FacetsDocId> fetchAllIds(String type) {
        if (type != FOLDER_DOC_TYPE) {
            return Collections.emptyIterator()
        }

        // XXX: temporary until core-api is extended to properly support controlled tags
        SQLQuery query = sessionFactory.currentSession.createSQLQuery '''
            SELECT folder_id from biomart_user.folder_study_mapping
            WHERE c_fullname IN (SELECT path from i2b2metadata.i2b2_tags WHERE tag_option_id IS NOT NULL)
                AND root IS TRUE
        '''

        query.list().collect {
            new FacetsDocId(FOLDER_DOC_TYPE, it as String)
        }.iterator()
    }

    @Override
    Set<FacetsDocument> collectDocumentsWithIds(Set<FacetsDocId> docIds) {
        // XXX: temporary until core-api is extended to properly support controlled tags
        SQLQuery query = sessionFactory.currentSession.createSQLQuery '''
            select folder_id, O.value, solr_field_name, TT.value_type, path
            from i2b2metadata.i2b2_tags T
            inner join biomart_user.folder_study_mapping FSM ON (T.path = FSM.c_fullname AND FSM.root IS TRUE)
            inner join i2b2metadata.i2b2_tag_options O ON (T.tag_option_id = O.tag_option_id)
            natural inner join i2b2metadata.i2b2_tag_types TT
            where folder_id in (:folders)
        '''

        query.setParameterList 'folders', docIds.collect { it.id as Long }
        query.list()
                .collect { it as List }
                .groupBy { it[0] /* folder id */ }
                .collect { rowsToDocument it.value } as Set
    }


    private FacetsDocument rowsToDocument(List<List<Object>> rows) {
        def builder = FacetsDocument.newFieldValuesBuilder()
        Long folderId = rows.find()[0]

        builder[FIELD_FOLDER_ID] = folderId
        builder[FIELD_CONCEPT_PATH] = rows.find()[4]
        rows.each {
            def (dummy, value, dbFieldName, dbFieldType) = it

            FacetsField field = buildFacetsField(dbFieldName, dbFieldType)
            if (!field) {
                return
            }
            if (field.type == FacetsFieldType.INTEGER && value?.isLong()) {
                value = value as Long
            }
            builder[field] = value
        }

        new FacetsDocument(
                facetsDocId: new FacetsDocId(FOLDER_DOC_TYPE, folderId as String),
                fieldValues: builder.build())
    }

    @Override
    FacetsFieldDisplaySettings getDisplaySettingsForIndex(String fieldName) {
        if (fieldName.length() < 3 || fieldName.toLowerCase(Locale.ENGLISH) != fieldName) {
            return null
        }

        // XXX: temporary until core-api is extended to properly support controlled tags
        SQLQuery query = sessionFactory.currentSession.createSQLQuery '''
            SELECT tag_type, index
            FROM i2b2metadata.i2b2_tag_types
            WHERE solr_field_name = :solr_field_name
                AND value_type = :value_type
        '''

        FacetsFieldType fieldType = FacetsFieldType.values().find { it.suffix == fieldName[-1] }
        if (!fieldType) {
            log.warn("Could not determine field type for field with name $fieldName")
        }

        query.setParameter 'solr_field_name', fieldName[0..-3]
        query.setParameter 'value_type', DB_FIELD_TYPE_MAPPING.inverse().get(fieldType)
        def res = query.list()
        if (res.empty) {
            return null
        }

        new SimpleFacetsFieldDisplaySettings(
                displayName: res*.getAt(0).join(' / '),
                hideFromListings: false,
                order: MANAGED_TAGS_FIELDS_BASE_PRECEDENCE + res*.getAt(1).min(),
        )
    }

    private static FacetsField buildFacetsField(String dbFieldName, String dbFieldType) {
        String textType = dbFieldType.toUpperCase(Locale.ENGLISH);
        if (DB_FIELD_TYPE_MAPPING[textType] == null) {
            log.warn("Saw unrecognized field type: $textType in i2b2_tag_types")
            return null
        }

        String ret = dbFieldName
        if (dbFieldName == FacetsIndexingService.FIELD_TEXT.fieldName) {
            return ret
        }

        if (dbFieldName.toLowerCase(Locale.ENGLISH) != dbFieldName) {
            log.warn("Saw unrecognized field name with uppercase " +
                    "letters: $dbFieldName in i2b2_tag_types")
        }

        def type = DB_FIELD_TYPE_MAPPING[textType]
        new FacetsFieldImpl(
                fieldName: dbFieldName + '_' + type.suffix,
                type: type)
    }
}
