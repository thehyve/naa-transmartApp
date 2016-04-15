//# sourceURL=rwgsearch.js

window.rwgModel = {
    jquery: jQuery({}),

    on: function rwgModel_on(eventName, func) {
        this.jquery.on(eventName, function() {
            /* don't call callback with event object as first argument */
            Array.prototype.shift.apply(arguments);
            func.apply(this, arguments);
        });
        return this;
    },
    onWithEvent: function rwgModel_on() {
        debugger;
        this.jquery.on.apply(this.jquery, arguments);
        return this;
    },
    trigger: function rwgModel_trigger() {
        if (arguments.length == 2 && arguments[1] instanceof Array) {
            arguments = [arguments[0], [arguments[1]]];
        }
        this.jquery.trigger.apply(this.jquery, arguments);
        return this;
    },

    /**
     * {
     *   operator: 'AND' | 'OR',
     *   fieldTerms: {
     *     <field name>: {
     *       operator: 'AND' | 'OR'
     *       searchTerms: [
     *         {
     *           literalTerm: <>,
     *           luceneTerm: <>,
     *         }
     *       ]
     *     }
     *   }
     */
    searchSpecification: {
        operator: 'AND',
        fieldTerms: {},
    },
    addSearchTerm: function rwgModel_addSearchTerm(fieldName, value, literal) {
        literal = literal || false;
        var fieldTerm = this.searchSpecification.fieldTerms[fieldName];
        var searchTerm;

        if (!fieldTerm) {
            fieldTerm = this.searchSpecification.fieldTerms[fieldName] = {
                operator: 'OR',
                searchTerms: [],
            };
        }
        if (literal) {
            searchTerm = { literalTerm: value };
        } else {
            searchTerm = { luceneTerm: value };
        }

        // don't do nothing if the term is already there
        var prev = fieldTerm.searchTerms.find(function(el) {
            return el.literalTerm == searchTerm.value &&
            el.luceneTerm == searchTerm.luceneTerm;
        });
        if (prev) {
            return;
        }

        fieldTerm.searchTerms.push(searchTerm);

        this.trigger('search_specification', this.searchSpecification);
    },
    removeSearchTerm: function rwgModel_removeSearchTerm(fieldName, value, literal) {
        var searchTerm;
        var fieldSpec = this.searchSpecification.fieldTerms[fieldName];
        if (!fieldSpec) {
            console.error('Cannot remove term for field, no such field with terms: ' + fieldName);
            return;
        }

        if (literal) {
            searchTerm = { literalTerm: value };
        } else {
            searchTerm = { luceneTerm: value };
        }


        fieldSpec.searchTerms = fieldSpec.searchTerms.filter(function(el) {
            return searchTerm.literalTerm != el.literalTerm ||
                searchTerm.luceneTerm != el.luceneTerm;
        });
        if (fieldSpec.searchTerms.length == 0) {
        	delete this.searchSpecification.fieldTerms[fieldName];
        }

        this.trigger('search_specification', this.searchSpecification);
    },
    alterSearchSpecification: function rwgModel_alterSearchSpecification(func) {
        // generic function for changes in searchSpecification,
        // with notification at the end
        func(this.searchSpecification);
        this.trigger('search_specification', this.searchSpecification);
    },
    clearSearchTerms: function rwgSearch_clearSearchTerms() {
        this.searchSpecification.fieldTerms = {};
        this.trigger('search_specification', this.searchSpecification);

        this.currentFilterResults = undefined;
        this.returnedConcepts = undefined;
        this.returnedFolders = undefined;
    },
    _returnedFolders: undefined, // ids only
    get returnedFolders() { return this._returnedFolders; },
    set returnedFolders(v) {
        this._returnedFolders = v;
        this.trigger('folder_list', v);
    },
    _returnedConcepts: undefined, // concept paths
    get returnedConcepts() { return this._returnedConcepts; },
    set returnedConcepts(v) {
        this._returnedConcepts = v;
        this.trigger('concepts_list', v);
    },

    _searchCategories: undefined, /* as gotten from service, map from field name to display name */
    get searchCategories() { return this._searchCategories; },
    set searchCategories(v) {
        this._searchCategories = v;
        this.trigger('search_categories', v);
    },
    _currentCategory: undefined, /* field name */
    get currentCategory() { return this._currentCategory; },
    set currentCategory(v) {
        this._currentCategory = v;
        this.trigger('current_category', v);
    },

    _startingFilterResults: undefined, /* as returned by getTopTerms */
    get startingFilterResults() { return this._startingFilterResults; },
    set startingFilterResults(v) {
        this._startingFilterResults = v;
        if (!this.currentFilterResults) {
            this.trigger('current_filters', v);
        }
    },
    _currentFilterResults: undefined,  /* same format; null to use startingFilterResults  */
    get currentFilterResults() { return this._currentFilterResults; },
    set currentFilterResults(v) {
    	var prevValue = this._currentFilterResults;
        this._currentFilterResults = v;
        if (v) { /* new current value */
            this.trigger('current_filters', v);
        } else if (prevValue) {
        	this.trigger('current_filters', this.startingFilterResults);
        }
    },

    serialize: function rwgModel_serialize() {
        var keys = ['searchCategories',
                    'currentCategory',
                    'startingFilterResults',
                    'searchSpecification'];
        var res = {};
        keys.forEach(function(k) { res[k] = this[k]; }.bind(this));
        return JSON.stringify(res);
    },
    unserialize: function rwgModel_unserialize(str) {
        try {
            var obj = JSON.parse(str);
        } catch (e) {
            console.error('Error reading rwgModel saved data', e);
            return;
        }
        Object.keys(obj).forEach(function(k) { this[k] = obj[k]; }.bind(this));
        this.trigger('search_specification', this.searchSpecification);
    },
};

window.rwgView = {
    searchCategoriesEl: /* #search-categories */ undefined,
    searchInputEl:      /* #search-ac */         undefined,
    filterBrowserEl:    /* #filter-browser */    undefined,
    boxSearchEl:        /* #box-search */        undefined,
    activeSearchEl:     /* #active-search-div */ undefined, // child of box-search
    globalOperatorEl:   /* #globaloperator */    undefined,
    clearFiltersEl:     /* #clearbutton */       undefined,

    init: function rwgView_init() {
        // find elements
        this.searchCategoriesEl = jQuery('#search-categories');
        this.searchInputEl      = jQuery('#search-ac');
        this.filterBrowserEl    = jQuery('#filter-browser');
        this.boxSearchEl        = jQuery('#box-search');
        this.activeSearchEl     = jQuery('#active-search-div');
        this.globalOperatorEl   = jQuery('#globaloperator');
        this.clearFiltersEl     = jQuery('#clearbutton');

        this.bindToModel();
        this.bindUIEvents();
        var loadedFromStorage = false;
        if (sessionStorage.getItem('rwgModel')) {
            storedRwgModel = sessionStorage.getItem('rwgModel');
            rwgModel.unserialize(storedRwgModel);
            loadedFromStorage = true;
        }
        jQuery(window).unload(function() {
            sessionStorage.setItem('rwgModel', rwgModel.serialize());
        });

        if (rwgModel.searchCategories === undefined) {
            rwgController.fetchCategories();
        }
        if (rwgModel.startingFilterResults === undefined) {
            rwgController.fetchStartingFilterResults();
        }
    },

    bindToModel: function rwgView_bindToModel() {
        rwgModel.on('current_category',     this.currentCategoryChanges.bind(this));
        rwgModel.on('search_categories',    this.searchCategoriesChange.bind(this));
        rwgModel.on('current_filters',      this.currentFilterResultsChange.bind(this));
        rwgModel.on('search_specification', this.searchSpecificationChanges.bind(this));
        rwgModel.on('search_specification', this.selectedFiltersChange.bind(this));
        rwgModel.on('search_specification', function(data) { rwgController.performSearch(data); });
    },
    bindUIEvents: function rwgView_bindUIEvents() {
        this.activeSearchBindEvents();
        this.filterItemsBindEvents();
    },

    searchCategoriesChange: function rwgView_searchCategoriesChange(searchCategories) {
        var addCategory = function addCategory(field, text) {
            var el = jQuery('<option></option>')
                .attr('value', field)
                .text(text);

            this.searchCategoriesEl.append(el)
        }.bind(this);

        for (var category in searchCategories) {
            addCategory(category, searchCategories[category]);
        }

        // bind events
        this.searchCategoriesEl.unbind('change');
        this.searchCategoriesEl.change(function() {
            rwgController.changeCurrentCategory(this.searchCategoriesEl.val());
        }.bind(this));

        (function rwgView_setupAutocomplete() {
            this.searchInputEl.autocomplete({
                position: { my: 'left top', at: 'left bottom', collision: 'none' },
                // source set in currentCategoryChanges
                minLength: 1,
                select: function (event, ui) {
                    rwgController.addSearchTerm(ui.item.category, ui.item.value, true);
                    this.searchInputEl.val('');
                    return false;
                }.bind(this),
            }).data("uiAutocomplete")._renderItem = function(ul, item) {
                var a = jQuery('<a>');
                var span = jQuery('<span>')
                    .text(searchCategories[item.category] + '>');
                var b = jQuery('<b>').text(item.value);
                a.append(span).append(document.createTextNode(' ')).append(b);

                return jQuery('<li>')
                    .data("item.autocomplete", item)
                    .append(a)
                    .appendTo(ul);
            };

            // Capture the enter key on the slider and fire off the search event on the autocomplete
            this.searchCategoriesEl.unbind('keypress');
            this.searchCategoriesEl.keypress(function(event) {
                if (event.which == 13) {
                    this.searchInputEl.autocomplete('search');
                }
            });

            this.searchInputEl.unbind('keypress');
            this.searchInputEl.keypress(function(event) {
                if (event.which != 13) {
                    return true;
                }

                rwgController.addSearchTerm(rwgModel.currentCategory, this.searchInputEl.val(), false);
                this.searchInputEl.val('');
                return false;
            }.bind(this));
        }.bind(this))();
    },
    currentCategoryChanges: function rwgView_currentCategoryChanges(currentCategory) {
        this.searchCategoriesEl.val(currentCategory);
        this.searchInputEl.autocomplete('option',
            'source', rwgURLs.rwgAutoComplete + "?category=" + encodeURIComponent(currentCategory));
    },
    currentFilterResultsChange: function rwgView_currentFilterResultsChange(currentResults) {
        function addField(fieldData) {
            var category = fieldData.category;
            var choices = fieldData.choices;
            var choices = fieldData.choices;
            var titleDiv = jQuery('<div>')
                .addClass('filtertitle')
                .attr('name', category.field)
                .text(category.displayName);

            var contentDiv = jQuery('<div>')
                .addClass('filtercontent')
                .data('fieldName', category.field)
                .hide();

            choices.forEach(function(ch) {
                var newItem = jQuery('<div></div>')
                    .addClass('filteritem')
                    .data('fieldName', category.field)
                    .data('value', ch.value)
                    .text(ch.value + ' (' + ch.count + ')');
                contentDiv.append(newItem);
            });

            this.filterBrowserEl.append(titleDiv);
            this.filterBrowserEl.append(contentDiv);
        }

		this.filterBrowserEl.find('.filtertitle, .filtercontent').remove();
        currentResults.forEach(addField.bind(this));
        this.selectedFiltersChange(rwgModel.searchSpecification);
        this.filterBrowserEl.removeClass('ajaxloading');
    },
    selectedFiltersChange: function rwgView_selectedFiltersChange(searchSpecification) {
        // clear current selected classes
        this.filterBrowserEl.find('.selected').each(function() {
            jQuery(this).removeClass('selected');
        });
        var filterTitles = this.filterBrowserEl.find('.filtertitle');

        Object.keys(searchSpecification.fieldTerms).forEach(function(fieldName) {
            var searchTerms = searchSpecification.fieldTerms[fieldName].searchTerms || [];
            var titleElement = filterTitles.filter(function() {
                return jQuery(this).attr('name') == fieldName;
            });
            if (titleElement.size() == 0) {
                return;
            }

            searchTerms.forEach(function(searchTerm) {
                titleElement
                    .next('.filtercontent')
                    .children('.filteritem')
                    .filter(function() {
                        var jq = jQuery(this);
                        return jq.data('value') == searchTerm.literalTerm;
                    })
                    .addClass('selected')
                    .parent() // also make sure the filter content is being show
                    .show();
            });
        });
    },
    filterItemsBindEvents: function rwgView_filterItemsBindEvents() {
        this.filterBrowserEl.on('click', '.filteritem', function () {
            var jq = jQuery(this);
            var selecting = !jq.hasClass('selected');
            if (selecting) {
                rwgController.addSearchTerm(jq.data('fieldName'), jq.data('value'), true);
            } else {
                rwgController.removeSearchTerm(jq.data('fieldName'), jq.data('value'), true);
            }
        });
        this.filterBrowserEl.on('click', '.filtertitle', function () {
            jQuery(this).next('.filtercontent').toggle('fast');
        });

    },

    searchSpecificationChanges: function rwgView_searchSpecificationChanges(searchSpecification) {
        if (searchSpecification.operator == 'AND') {
            this.globalOperatorEl.attr('class', 'andor and');
        } else {
            this.globalOperatorEl.attr('class', 'andor or');
        }

        var elements = [];
        function createSpacer(operator) {
            return jQuery('<span>')
                .addClass('spacer')
                .text(operator == 'AND' ? 'and ' : 'or ')
        }
        function addCategory(fieldName, specs) {
            if (elements.length > 0) { // not the first category
                var separator = jQuery('<span>')
                    .addClass('category_join')
                    .append(document.createTextNode(searchSpecification.operator))
                    .append(jQuery('<span class="h_line"></span>'))
                elements.push(separator);
            }

            var title = jQuery('<span>')
                .addClass('category_label')
                .text(rwgModel.searchCategories[fieldName] + '\u00A0>');
            elements.push(title, document.createTextNode('\u00A0'));

            specs.searchTerms.forEach(function(term, i) {
                if (i > 0) {
                    elements.push(createSpacer(specs.operator));
                }

                var value = term.literalTerm ? term.literalTerm : term.luceneTerm;
                var term = jQuery('<span>')
                    .addClass('term')
                    .addClass(term.literalTerm ? 'literal-term' : 'lucene-term')
                    .text((term.literalTerm ? term.literalTerm : term.luceneTerm) + '\u00A0')
                    .append(
                        jQuery('<a href="#">')
                            .addClass('term-remove')
                            .data('termValue', value)
                            .data('termLiteral', term.literalTerm ? true : false)
                            .data('fieldName', fieldName)
                            .append(jQuery('<img alt="remove">')
                                .attr('src', window.rwgURLs.crossImage)
                                .data('fieldName', fieldName)))
                    .append(document.createTextNode('\u00A0'))
                elements.push(term);
            });
            if (specs.searchTerms.length > 1) {
                elements.push(
                    jQuery('<div>')
                        .addClass('andor')
                        .addClass(specs.operator == 'AND' ? 'and' : 'or')
                        .data('fieldName', fieldName)
                        .text('\u00A0'));
            }
        }

        Object.keys(searchSpecification.fieldTerms).forEach(function(fieldName) {
            addCategory.call(this, fieldName, searchSpecification.fieldTerms[fieldName]);
        }.bind(this));

        this.activeSearchEl.empty();
        this.activeSearchEl.append(elements);
    },
    
    activeSearchBindEvents: function rwgView_activeSearchBindEvents() {
        this.boxSearchEl.on('click', '.andor', function() {
            var jq = jQuery(this);
            if (jq.attr('id') == 'globaloperator') {
                rwgController.switchGlobalOperator();
            } else {
                rwgController.switchFieldOperator(jq.data('fieldName'));
            }
        });

        this.boxSearchEl.on('click', '.term-remove', function() {
            var jq = jQuery(this);
            rwgController.removeSearchTerm(jq.data('fieldName'), jq.data('termValue'), jq.data('termLiteral'));
        });

        this.clearFiltersEl.click(rwgController.clearSearchTerms.bind(rwgController));
    },
};

window.rwgController = {
    flyingSearch: undefined,

    fetchCategories: function rwgController_fetchCategories() {
        jQuery.ajax({
            url: window.rwgURLs.getSearchCategories,
            dataType: 'json',
        }).then(function(json) {
            var data = { 'ALL': 'All' };
            jQuery.extend(data, json);
            rwgModel.searchCategories = data;
            rwgModel.currentCategory = 'ALL'; /* reset field after fetching categories */
        }).fail(function(jqhr) { console.error('Failed getting search categories', jqhr); });
    },
    fetchStartingFilterResults: function rwgController_fetchStartingFilterResults() {
        jQuery.ajax({
            url: window.rwgURLs.getFilterCategories,
            dataType: 'json',
        }).then(function(json) {
            rwgModel.startingFilterResults = json;
        }).fail(function(jqhr) { console.error('Failed getting filter categories', jqhr); })
    },
    changeCurrentCategory: function rwgController_changeCurrentCategory(fieldName) {
        rwgModel.currentCategory = fieldName;
    },
    addSearchTerm: function rwgController_addSearchterm(fieldName, value, literal) {
        rwgModel.addSearchTerm(fieldName, value, literal);
    },
    removeSearchTerm: function rwgController_removeSearchTerm(fieldName, value, literal) {
        rwgModel.removeSearchTerm(fieldName, value, literal);
    },
    clearSearchTerms: function rwgController_clearSearchTerms() {
        rwgModel.clearSearchTerms();
    },
    switchGlobalOperator: function rwgController_switchGlobalOperator() {
        rwgModel.alterSearchSpecification(function(spec) {
            spec.operator = spec.operator === 'AND' ? 'OR' : 'AND';
        });
    },
    switchFieldOperator: function rwgController_switchFieldOperator(fieldName) {
        rwgModel.alterSearchSpecification(function(spec) {
            var fieldSpec = spec.fieldTerms[fieldName];
            if (fieldSpec === undefined) {
                console.error('Try to swap field operator for field with no search terms: ' + fieldName);
                return;
            }
            fieldSpec.operator = fieldSpec.operator === 'AND' ? 'OR' : 'AND';
        });
    },
    performSearch: function rwgController_performSearch(searchSpecification) {
        if (this.flyingSearch) {
            this.flyingSearch.abort();
        }

        if (Object.keys(searchSpecification.fieldTerms).length == 0) {
        	// nothing to search
        	rwgModel.returnedFolders = undefined;
        	rwgModel.returnedConcepts = undefined;
        	rwgModel.currentFilterResults = undefined;

            window.GLOBAL.PathToExpand = '';
            getCategories();
        	return;
        }

        this.flyingSearch = jQuery.ajax({
            method: 'POST',
            url: window.rwgURLs.getFacetResults,
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(searchSpecification),
        }).then(function getFacetResults_success(json) {
            rwgModel.returnedFolders = json['folderIds'];
            rwgModel.returnedConcepts = json['conceptKeys'];
            rwgModel.currentFilterResults = json['facets'];
            // create new list with concepts that are a prefix of some other
            // this is called 'unique leaves' in the codebase, even though
            // they are not necessarily leaves
            var uniqueLeaves = json['conceptKeys'].filter(function(el) {
                // keep only if there's no other element for which this is a prefix
                return !json['conceptKeys'].find(function(it) { return el != it && it.startsWith(el); });
            });
            if (window.searchByTagComplete) {
                // dataset explorer
                searchByTagComplete({
                    searchResults: json['conceptKeys'],
                    uniqueLeaves: uniqueLeaves,
                });
            }
        }).fail(function(jqhr) {
            console.error('Failed getting search categories', jqhr); }
        ).always(function() { this.flyingSearch = undefined;}.bind(this));
    }
};

var currentCategories = new Array();
var currentSearchOperators = new Array(); //AND or OR - keep, in line with currentCategories
var currentSearchTerms = new Array(); 

// Store the nodes that were selected before a new node was selected, so that we can compare to the nodes that are selected after.  Selecting
//  one node in the tree can cause lots of changes in other parts of the tree (copies of this node change, children/parents change, 
//  parents of parents, children of parents of parent, etc.)
var nodesBeforeSelect = new Array();

// By default, allow the onSelect event to trigger for the tree nodes;  However, we don't want select events that are triggered from inside the onSelect
// event to cause the onSelectEvent code to keep triggering itself.  So change this to false before any call to select() within the onSelect (the event
// will still fire but is stopped immediately); and set this flag back to true at the end of the event so it can be triggered again.  
var allowOnSelectEvent = true;

// Method to add the categories for the select box
function addSelectCategories()	{
	
	if (sessionSearchCategory == "") { sessionSearchCategory = "ALL"; }
	
	jQuery("#search-categories").append(jQuery("<option></option>").attr("value", "ALL").text("All").attr('id', 'allCategory'));
	
	jQuery("#search-categories").change(function() {
		jQuery('#search-ac').autocomplete('option', 'source', sourceURL + "?category=" + this.options[this.selectedIndex].value);
		jQuery.ajax({
			url:updateSearchCategoryURL,
			data: {id: jQuery("#search-categories").val()}
		});
	});
	
	jQuery.getJSON(getCategoriesURL, function(json) {
		for (category in json) {
			jQuery("#search-categories")
				.append(
					jQuery("<option></option>")
						.attr("value", category)
						.text(json[category]));
		}
		
		jQuery("#search-categories").val(sessionSearchCategory);
		jQuery('#search-ac').autocomplete('option', 'source', sourceURL + "?category=" + jQuery('#search-categories').val());
    });
}

function addFilterCategories() {
	jQuery.getJSON(getFilterCategoriesURL, function(json) {
		for (var i=0; i<json.length; i++)	{
			var category = json[i].category;
			var choices = json[i].choices;
			var titleDiv = jQuery("<div></div>").addClass("filtertitle").attr("name", category.field).text(category.displayName);
			var contentDiv = jQuery("<div></div>").addClass("filtercontent").attr("name", category.field).attr("style", "display: none");
			for (var j=0; j < choices.length; j++) {
				var choice = choices[j];
				
				var newItem = jQuery("<div></div>")
					.addClass("filteritem")
					.attr("name", category.field)
					.text(choice.value + ' (' + choice.count + ')');
				
				//If this has been selected, highlight it
				var idString = '[id="' + category.displayName + "|" + category.field + ";" + choice.value + '"]';
				idString = idString.replace(/,/g, "%44").replace(/&/g, "%26"); //Replace commas and ampersands
				var element = jQuery(idString);
				if (element.size() > 0) {
					newItem.addClass("selected");
				}

				contentDiv.append(newItem);
			}
			jQuery("#filter-browser").append(titleDiv);
			jQuery("#filter-browser").append(contentDiv);
		}
		
		jQuery("#filter-browser").removeClass("ajaxloading");
    });
}

//Method to add the autocomplete for the search keywords
function addSearchAutoComplete()	{
	jQuery("#search-ac").autocomplete({
		position:{my:"left top",at:"left bottom",collision:"none"},
		source: sourceURL,
		minLength:1,
		select: function(event, ui) {  
			searchParam={id:ui.item.id,display:ui.item.category,keyword:ui.item.label,category:ui.item.categoryId};
			addSearchTerm(searchParam);
			
			//If category is ALL, add this as free text as well
			var category = jQuery("#search-categories").val();
			return false;
		}
	}).data("autocomplete")._renderItem = function( ul, item ) {
		var resulta = '<a><span class="category-' + item.category.toLowerCase() + '">' + item.category + '&gt;</span>&nbsp;<b>' + item.label + '</b>&nbsp;';
		if (item.synonyms != null) {
			resulta += (item.synonyms + '</a>');
		}
		else {
			resulta += '</a>';
		}
		
		return jQuery('<li></li>')		
		  .data("item.autocomplete", item )
		  .append(resulta)
		  .appendTo(ul);
	};	
		
	// Capture the enter key on the slider and fire off the search event on the autocomplete
	jQuery("#search-categories").keypress(function(event)	{
		if (event.which == 13)	{
			jQuery("#search-ac").autocomplete('search');
		}
	});
	
	jQuery('#search-ac').keypress(function(event) {
		var category = jQuery("#search-categories").val();
		var categoryText = jQuery('#search-categories option:selected').text();
		if (event.which == 13 && (category == 'DATANODE' || category == 'text' || category == 'ALL')) {
			var val = jQuery('#search-ac').val();
			if (category == 'ALL') {category = 'text'; categoryText = 'Free Text';}
			searchParam={id:val,display:categoryText,keyword:val,category:category};
			addSearchTerm(searchParam);
			return false;
            jQuery('#search-ac').empty();
		}
	});
	return false;
}

//Add the search term to the array and show it in the panel.
function addSearchTerm(searchTerm, noUpdate, openInAnalyze,datasetExplorerPath)	{
	var category = searchTerm.display == undefined ? "TEXT" : searchTerm.display;
	
	category = category + "|" + (searchTerm.category == undefined ? "TEXT" : searchTerm.category);
	
	var text = (searchTerm.text == undefined ? (searchTerm.keyword == undefined ? searchTerm : searchTerm.keyword) : searchTerm.text);
	var id = searchTerm.id == undefined ? -1 : searchTerm.id;
	var key = category + ";" + text + ";" + id;
	if (currentSearchTerms.indexOf(key) < 0)	{
		currentSearchTerms.push(key);
		if (currentCategories.indexOf(category) < 0)	{
			currentCategories.push(category);
			currentSearchOperators.push("or");
		}
	} 
	
	// clear the search text box
	jQuery("#search-ac").val("");

	// only refresh results if the tree was not updated (the onSelect also fires these event, so don't want to do 2x)
	
	if (!noUpdate) {
		if(!openInAnalyze){
			jQuery.ajax({
				url:resetNodesRwgURL
			});
			showSearchTemplate();
		}
	  showSearchResults(openInAnalyze, datasetExplorerPath);
	}
}

//Main method to show the current array of search terms 
function showSearchTemplate()	{
	var searchHTML = '';
	var startATag = '&nbsp;<a id=\"';
	var endATag = '\" class="term-remove" href="#" onclick="removeSearchTerm(this);">';
	var imgTag = '<img alt="remove" src="' + crossImageURL + '"/></a>&nbsp;'
	var firstItem = true;
	var needsToggle = false;
	var geneTerms = 0;
	
	var globalLogicOperator = "AND";
	if (jQuery('#globaloperator').hasClass("or")) { globalLogicOperator = "OR" }

	// iterate through categories array and move all the "gene" categories together at the top 
	var newCategories = new Array();
	var newSearchOperators = new Array();
	
	var geneCategoriesProcessed = false;
	var geneCategories = 0;
	
	for (var i=0; i<currentCategories.length; i++)	{
		var catFields = currentCategories[i].split("|");
		var catId = catFields[1];
		
		// when we find a "gene" category, add it and the rest of the "gene" categories to the new array
		if (isGeneCategory(catId)) {
			geneCategories++;
			// first check if we've processed "gene" categories yet
			if (!geneCategoriesProcessed)  {
				
				// add first gene category to new array
				newCategories.push(currentCategories[i]);
				newSearchOperators.push(currentSearchOperators[i]);

				// look for other "gene" categories, starting at the next index value, and add each to array
				for (var j=i+1; j<currentCategories.length; j++)	{
					var catFields2 = currentCategories[j].split("|");
					var catId2 = catFields2[1];
					if (isGeneCategory(catId2)) {
						newCategories.push(currentCategories[j]);
						newSearchOperators.push(currentSearchOperators[j]);
					}				
				}
				// set flag so we don't try to process again
				geneCategoriesProcessed = true;
			}
		}
		else  {    // not a gene catageory, add to new list
			newCategories.push(currentCategories[i]);
			newSearchOperators.push(currentSearchOperators[i]);
		}
	}
	
	// replace old array with new array
    currentCategories = newCategories;
    currentSearchOperators = newSearchOperators;
	
	for (var i=0; i<currentCategories.length; i++)	{
		for (var j=0; j<currentSearchTerms.length; j++)	{
			var fields = currentSearchTerms[j].split(";");
			if (currentCategories[i] == fields[0]){
				var tagID = currentSearchTerms[j].replace(/,/g, "%44").replace(/&/g, "%26");	// URL encode a few things
				
				var catFields = fields[0].split("|");
				var catDisplay = catFields[0];
				var catId = catFields[1];
				
				if (isGeneCategory(catId)) {
					geneTerms++;
				}

				if (firstItem)	{
					
					if (i>0)	{	
						
						var suppressAnd = false;
						// if this is a "gene" category, check the previous category and see if it is also one
		                if (isGeneCategory(catId))  {
							var catFieldsPrevious = currentCategories[i-1].split("|");
							var catIdPrevious = catFieldsPrevious[1];
		                	if (isGeneCategory(catIdPrevious))  {
		                		suppressAnd = true;	
		                	}
		                } 
						
		                // if previous category is a "gene" category, don't show operator
		                if (!suppressAnd)  {
							searchHTML = searchHTML + "<span class='category_join'>" + globalLogicOperator + "<span class='h_line'></span></span>";  			// Need to add a new row and a horizontal line
					    }
		                else  {
							searchHTML = searchHTML + "<br/>";  				                	
		                }
					}
					searchHTML = searchHTML +"<span class='category_label'>" +catDisplay + "&nbsp;></span>&nbsp;<span class=term>"+ fields[1] + startATag + tagID + endATag + imgTag +"</span>";
					firstItem = false;
				}
				else {
					searchHTML = searchHTML + "<span class='spacer'>" + currentSearchOperators[i] + " </span><span class=term>"+ fields[1] + startATag + tagID + endATag + imgTag +"</span> ";
					needsToggle = true;
				}			
			}
			else {
				continue; // Do the categories by row and in order
			}
		}
		//Show the and/or toggle, if this is a non-gene category or any gene category but the last.
		if ((!isGeneCategory(catId) && needsToggle) || i == geneCategories-1 && geneTerms > 1)  {
			searchHTML = searchHTML + "<div name='" + i + "' class='andor " + currentSearchOperators[i] + "'>&nbsp;</div>";
		}
		firstItem = true;
		needsToggle = false;
	}
	document.getElementById('active-search-div').innerHTML = searchHTML;
	getSearchKeywordList();
}

//Method to load the search results in the search results panel and facet counts into tree
//This occurs whenever a user add/removes a search term
function showSearchResults(openInAnalyze, datasetExplorerPath)	{

	// clear stored probe Ids for each analysis
	analysisProbeIds = new Array();  
	
	// clear stored analysis results
	jQuery('body').removeData();
	
	jQuery('#results-div').empty();
	
	// call method which retrieves facet counts and search results
	showFacetResults(openInAnalyze, datasetExplorerPath);
	
	//all analyses will be closed when doing a new search, so clear this array
	openAnalyses = [];

}

//Method to load the facet results in the search tree and populate search results panel
function showFacetResults(openInAnalyze, datasetExplorerPath)	{
	if(openInAnalyze == undefined){
		openInAnalyze = false;
	}
	var globalLogicOperator = "AND";
	if (jQuery('#globaloperator').hasClass("or")) { globalLogicOperator = "OR" }
	
	var savedSearchTermsArray;
	var savedSearchTerms;
	
	if (currentSearchTerms.toString() == '')
		{
			savedSearchTermsArray = new Array();
			savedSearchTerms = '';
		
		}
	else
		{
			savedSearchTerms = currentSearchTerms.join(",,,");
			savedSearchTermsArray = savedSearchTerms.split(",,,");
		}
	
	// Generate list of categories/terms to send to facet search
	// create a string to send into the facet search, in form Cat1:Term1,Term2&Cat2:Term3,Term4,Term5&...

	var facetSearch = new Array();   // will be an array of strings "Cat1:Term1|Term2", "Cat2:Term3", ...   
	var categories = new Array();    // will be an array of categories "Cat1","Cat2"
	var terms = new Array();         // will be an array of strings "Term1|Term2", "Term3"
	var operators = new Array();

	// first, loop through each term and add categories and terms to respective arrays 		
    for (var i=0; i<savedSearchTermsArray.length; i++)	{
		var fields = savedSearchTermsArray[i].split(";");
		// search terms are in format <Category Display>|<Category>:<Search term display>:<Search term id>
		var termId = fields[2]; 
		var categoryFields = fields[0].split("|");
		var category = categoryFields[1].replace(" ", "_");   // replace any spaces with underscores (these will then match the SOLR field names) 
		
		var categoryIndex = categories.indexOf(category);

		// if category not in array yet, add category and term to their respective array, else just append term to proper spot in its array
		if (categoryIndex == -1)  {
		    categories.push(category);
		    
		    //Get the operator for this category from the global arrays
		    var operatorIndex = currentCategories.indexOf(fields[0]);
		    var operator = currentSearchOperators[operatorIndex];
		    if (operator == null) { operator = 'or' }
		    operators.push(operator);
		    

		    terms.push(termId);
		}
		else  {
		    terms[categoryIndex] = terms[categoryIndex] + "|" + termId; 			
		}
	}

    // now construct the facetSearch array by concatenating the values from the cats and terms array
    for (var i=0; i<categories.length; i++)	{
    	var queryType = "";

    	queryType = "q";
    	facetSearch.push(queryType + "=" + categories[i] + ":" + encodeURIComponent(terms[i]) + "::" + operators[i]);
    }
    
	jQuery("#results-div").addClass('ajaxloading').empty();
    
    var queryString = facetSearch.join("&");
    
    //Construct a list of the current categories and operators to save
    var operators = [];
    for (var i=0; i < currentCategories.length; i++) {
    	var category = currentCategories[i];
    	var operator = currentSearchOperators[i];
    	operators.push(category + "," + operator);
    }
    var operatorString = operators.join(";");
    
    queryString += "&searchTerms=" + encodeURIComponent(savedSearchTerms) + "&searchOperators=" + operatorString + "&globaloperator=" + globalLogicOperator;
    
    if(!openInAnalyze){
	    if (searchPage == 'RWG') {
			jQuery.ajax({
				url:facetResultsURL,
				data: queryString + "&page=RWG",
				success: function(response) {
						jQuery('#results-div').removeClass('ajaxloading').html(response);
						checkSearchLog();
						updateAnalysisData(null, false);
						 displayResultsNumber();
				},
				error: function(xhr) {
					console.log('Error!  Status = ' + xhr.status + xhr.statusText);
				}
			});
	    }
	    else {
	    	//If there are no search terms, pass responsibility on to getCategories - if not, do our custom search
	    	if (savedSearchTermsArray.length == 0) {
	    		//Need to silently clear the search map here as well
				jQuery.ajax({url:clearSearchFilterURL});
				GLOBAL.PathToExpand = '';
	    		getCategories();
	    	}
	    	else {
	    		jQuery.ajax({
	    			url:facetResultsURL,
	    			data: queryString + "&page=datasetExplorer",
	    			success: function(response) {
	    			searchByTagComplete(response);
	    			checkSearchLog();
	    			},
	    			error: function(xhr) {
	    			console.log('Error! Status = ' + xhr.status + xhr.statusText);
	    			}
	    			});
	    	}
	    }
	}else{
		jQuery.ajax({
			url:saveSearchURL,
			data: queryString + "&page=RWG",
			success: function(response) {
				window.location.href = datasetExplorerPath;
			},
			error: function(xhr) {
				console.log('Error!  Status = ' + xhr.status + xhr.statusText);
				window.location.href = datasetExplorerPath;
			}
		});
	}

}

function isGeneCategory(catId)  {
	if ((catId == 'GENE') || (catId == 'PATHWAY') || (catId == 'GENELIST') || (catId == 'GENESIG')) {
		return true;
	}
	else  {
		return false;
	}
}

//retrieve the current list of search keyword ids
function getSearchKeywordList()   {

	var keywords = new Array();
	
	for (var j=0; j<currentSearchTerms.length; j++)	{
		var fields = currentSearchTerms[j].split(";");		
	    var keyword = fields[2];			
		keywords.push(keyword);
	}
	
	return keywords;
}

//Remove the search term that the user has clicked.
function removeSearchTerm(ctrl)	{
	jQuery.ajax({
		url:resetNodesRwgURL
	});
	var currentSearchTermID = ctrl.id.replace(/\%20/g, " ").replace(/\%44/g, ",").replace(/\%26/g, "&");
	var idx = currentSearchTerms.indexOf(currentSearchTermID);
	if (idx > -1)	{
		currentSearchTerms.splice(idx, 1);
		
		// check if there are any remaining terms for this category; remove category from list if none
		var fields = currentSearchTermID.split(";");
		var category = fields[0];
		clearCategoryIfNoTerms(category);

	}
	
	// Call back to the server to clear the search filter (session scope)
	jQuery.ajax({
		type:"POST",
		url:newSearchURL
	});

	// create flag to track if tree was updated
	var treeUpdated = false

	// only refresh results if the tree was not updated (the onSelect also fires these event, so don't want to do 2x)
	if (!treeUpdated) {
      showSearchTemplate();
	  showSearchResults();
	}
	
	//Remove selected status from filter browser for this item
	unselectFilterItem(fields[2]);
	
}

//Clear the tree, results along with emptying the two arrays that store categories and search terms.
function clearSearch()	{
	goWelcome();
	jQuery.ajax({
		url:resetNodesRwgURL
	});
	
	openAnalyses = []; //all analyses will be closed, so clear this array
	
	
	jQuery("#search-ac").val("");
	
	currentSearchTerms = new Array();
	currentCategories = new Array();
	currentSearchOperators = new Array();
	
	// Change the category picker back to ALL and set autocomplete to not have a category (ALL by default)
	document.getElementById("search-categories").selectedIndex = 0;
	jQuery('#search-ac').autocomplete('option', 'source', sourceURL);

	showSearchTemplate();
	showSearchResults(); //reload the full search results
	
}

//update a node's count (not including children)
function updateNodeIndividualFacetCount(node, count) {
	// only add facet counts if not a category 
	if (!node.data.isCategory)   {
		// if count is passed in as -1, reset the facet count to the initial facet count
		if (count > -1)  {
	        node.data.facetCount = count;
	    }
	    else  {
	    	node.data.facetCount = node.data.initialFacetCount
	    }
	    node.data.title = node.data.termName + " (" + node.data.facetCount + ")";	
	}
	else  {
	    node.data.facetCount = -1;
	    node.data.title = node.data.termName;	
	}
}

//Remove the category from current categories list if there are no terms left that belong to it
function clearCategoryIfNoTerms(category)  {
	
	var found = false;
	for (var j=0; j<currentSearchTerms.length; j++)	{
		var fields2 = currentSearchTerms[j].split(";");
		var category2 = fields2[0];
		
		if (category == category2)  {
			found = true; 
			break;
		}
	}
	
	if (!found)  {
		var index = currentCategories.indexOf(category);
		currentCategories.splice(index, 1);
		currentSearchOperators.splice(index, 1);
	}
}

function unselectFilterItem(id) {
	//Longhand as may contain : characters
	jQuery("[id='" + id + "']").removeClass('selected');
}

// ---

jQuery(document).ready(function() {

	jQuery('#sidebartoggle').click(function() {
		toggleSidebar();
    });
	
	
	
	//jQuery('#filter-browser').on('click', '.filtertitle', function () {
	//	jQuery('.filtercontent[name="' + jQuery(this).attr('name') + '"]').toggle('fast');
	//});
	//
	
	//jQuery('#filter-browser').on('click', '.filteritem', function () {
	//	var selecting = !jQuery(this).hasClass('selected');
	//	jQuery(this).toggleClass('selected');
	//
	//	var name = jQuery(this).attr('name');
	//	var id = jQuery(this).attr('id');
	//	var category = jQuery('.filtertitle[name="' + name + '"]').text();
	//	var value = jQuery(this).text();
	//
	//	//If selecting this filter, add it to the list of current filters
	//	if (selecting) {
	//		var searchParam={id:id,
	//		        display:category,
	//		        keyword:value,
	//		        category:name};
	//
	//		addSearchTerm(searchParam);
	//	}
	//	else {
	//		var idString = '[id="' + category + "|" + name + ";" + value + ";" + id + '"]';
	//		idString = idString.replace(/,/g, "%44").replace(/&/g, "%26"); //Replace special characters!
	//		var element = jQuery(idString);
	//		removeSearchTerm(element[0]);
	//	}
	//});
	
    jQuery('body').on('mouseenter', '.folderheader', function() {
		jQuery(this).find('.foldericonwrapper').fadeIn(150);
	});

    jQuery('body').on('mouseleave', '.folderheader', function() {
		jQuery(this).find('.foldericonwrapper').fadeOut(150);
	});

    jQuery('body').on('click', '.foldericon.add', function() {
		var id = jQuery(this).attr('name');
		jQuery(this).removeClass("foldericon").removeClass("add").removeClass("link").text("Added to cart");
		jQuery('#cartcount').hide();
		
		jQuery.ajax({
			url:exportAddURL,
			data: {id: id},			
			success: function(response) {
				jQuery('#cartcount').show().text(response);
			},
			error: function(xhr) {
				jQuery('#cartcount').show();
			}
		});
	});

    jQuery('body').on('click', '.foldericon.addall', function() {
		var nameelements = jQuery(this).closest('table').find('.foldericon.add');
		var ids = [];
		for (i = 0; i < nameelements.size(); i++) {
			ids.push(jQuery(nameelements[i]).attr('name'));
			jQuery(nameelements[i]).removeClass("foldericon").removeClass("add").removeClass("link").text("Added to cart");
		}
		
		jQuery('#cartcount').hide();
		
		jQuery.ajax({
			url:exportAddURL,
			data: {id: ids.join(",")},			
			success: function(response) {
				jQuery('#cartcount').show().text(response);
			},
			error: function(xhr) {
				jQuery('#cartcount').show();
			}
		});
	});
    
    jQuery('body').on('click', '.foldericon.delete', function() {
		var id = jQuery(this).attr('name');
		
		if (confirm("Are you sure you want to delete this file?")) {
			jQuery.ajax({
				url:deleteFileURL,
				data: {id: id},
				success: function(response) {
					jQuery('#files-table').html(response);
					//Get document count and reduce by 1
					var folderId = jQuery('#file-list-table').attr('name');
					var documentCount = jQuery('#folder-header-' + folderId + ' .document-count');
					if (documentCount.size() > 0) {
						var currentValue = documentCount.text();
						documentCount.text(currentValue - 1);
					}
				},
				error: function(xhr) {
					alert(xhr.message);
				}
			});
		}
	});

    jQuery('body').on('click', '.foldericon.view', function() {
	    var id = jQuery(this).closest(".folderheader").attr('name');
    	showDetailDialog(id);
	});
	
	jQuery('#metadata-viewer').on('click', '.editmetadata', function() {

    	var id = jQuery(this).attr('name');

		jQuery('#editMetadataOverlay').fadeIn();
		jQuery('#editMetadata').empty().addClass('ajaxloading');

		jQuery.ajax({
			url:editMetaDataURL,
			data: {folderId: id},			
			success: function(response) {
				jQuery('#editMetadata').html(response).removeClass('ajaxloading');
			},
			error: function(xhr) {
				alert(xhr.responseText);
				jQuery('#editMetadata').html(response).removeClass('ajaxloading');
			}
		});
	});
	
    //jQuery('#box-search').on('click', '.andor', function() {
    	//
    	//if (jQuery(this).attr('id') == 'globaloperator') {
    	//	//For global switch, just alter the class - this is picked up later
    	//    if (jQuery(this).hasClass("or")) {
    	//    	jQuery(this).removeClass("or").addClass("and");
    	//    }
    	//    else {
    	//    	jQuery(this).removeClass("and").addClass("or");
    	//    }
    	//    showSearchTemplate();
    	//    showSearchResults();
    	//}
    	//else {
    	//	//For individual categories, alter this index of the current search operators, then redisplay
	//	    if (jQuery(this).hasClass("or")) {
	//	    	currentSearchOperators[jQuery(this).attr('name')] = 'and';
	//	    }
	//	    else {
	//	    	currentSearchOperators[jQuery(this).attr('name')] = 'or';
	//	    }
	//	    showSearchTemplate();
	//	    showSearchResults();
    	//}
	//});
    	

	jQuery('#metadata-viewer').on('click', '.addassay', function() {

    	var id = jQuery(this).attr('name');

		jQuery('#createAssayOverlay').fadeIn();
		jQuery('#createAssay').empty().addClass('ajaxloading');
		jQuery('#editMetadata').empty();

		jQuery.ajax({
			url:createAssayURL,
			data: {folderId: id},			
			success: function(response) {
				jQuery('#createAssay').html(response).removeClass('ajaxloading');
			},
			error: function(xhr) {
				alert(xhr);
				jQuery('#createAssay').html(response).removeClass('ajaxloading');
			}
		});
	});

	jQuery('#metadata-viewer').on('click', '.addanalysis', function() {

    	var id = jQuery(this).attr('name');

		jQuery('#createAnalysisOverlay').fadeIn();
		jQuery('#createAnalysis').empty().addClass('ajaxloading');
		jQuery('#editMetadata').empty();

		jQuery.ajax({
			url:createAnalysisURL,
			data: {folderId: id},			
			success: function(response) {
				jQuery('#createAnalysis').html(response).removeClass('ajaxloading');
			},
			error: function(xhr) {
				alert(xhr);
				jQuery('#createAnalysis').html(response).removeClass('ajaxloading');
			}
		});
	});

	jQuery('#metadata-viewer').on('click', '.addfolder', function() {

    	var id = jQuery(this).attr('name');

		jQuery('#createFolderOverlay').fadeIn();
		jQuery('#createFolder').empty().addClass('ajaxloading');
		jQuery('#editMetadata').empty();

		jQuery.ajax({
			url:createFolderURL + "?",
			data: {folderId: id},			
			success: function(response) {
				jQuery('#createFolder').html(response).removeClass('ajaxloading');
			},
			error: function(xhr) {
				alert(xhr);
				jQuery('#createFolder').html(response).removeClass('ajaxloading');
			}
		});
	});
	
	jQuery('#metadata-viewer').on('click', '.deletefolder', function() {

    	var id = jQuery(this).attr('name');
    	var parent = jQuery(this).data('parent');
    	
    	if (confirm("Are you sure you want to delete this folder and the files and folders beneath it?")) {
			jQuery.ajax({
				url:deleteFolderURL,
				data: {id: id},
				success: function(response) {
					updateFolder(parent);
					showDetailDialog(parent);
					jQuery('.result-folder-name').removeClass('selected');
					jQuery('#result-folder-name-' + parent).addClass('selected');
				},
				error: function(xhr) {
					alert(xhr.message);
				}
			});
    	}
	});

	jQuery('#metadata-viewer').on('click', '.addstudy', function() {

    	var id = jQuery(this).attr('name');

		jQuery('#createStudyOverlay').fadeIn();
		jQuery('#createStudy').empty().addClass('ajaxloading');
		jQuery('#editMetadata').empty();

		jQuery.ajax({
			url:createStudyURL,
			data: {folderId: id},			
			success: function(response) {
				jQuery('#createStudy').html(response).removeClass('ajaxloading');
			},
			error: function(xhr) {
				alert(xhr);
				jQuery('#createStudy').html(response).removeClass('ajaxloading');
			}
		});
	});

	jQuery('#welcome-viewer').on('click', '.addprogram', function() {
		
	   	var id = jQuery(this).attr('name');

		jQuery('#createProgramOverlay').fadeIn();
		jQuery('#createProgram').empty().addClass('ajaxloading');
		jQuery('#editMetadata').empty();

		jQuery.ajax({
			url:createProgramURL,
			data: {folderId: id},			
			success: function(response) {
				jQuery('#createProgram').html(response).removeClass('ajaxloading');
			},
			error: function(xhr) {
				alert(xhr);
				jQuery('#createProgram').html(response).removeClass('ajaxloading');
			}
		});
	});

    jQuery('#exportOverlay').on('click', '.greybutton.remove', function() {

    	var row = jQuery(this).closest("tr");
	    var id = row.attr('name');
	   
	    jQuery('#cartcount').hide();
	    
		jQuery.ajax({
			url:exportRemoveURL,
			data: {id: id},			
			success: function(response) {
				row.remove();
				jQuery('#cartcount').show().text(response);
				updateExportCount();
				jQuery('#metadata-viewer').find(".exportaddspan[name='" + id + "']").addClass("foldericon").addClass("add").addClass("link").text('Add to export');
			},
			error: function(xhr) {
				jQuery('#cartcount').show();
			}
		});
	});

    jQuery('#exportOverlay').on('click', '.greybutton.export', function() {

    	var checkboxes = jQuery('#exporttable input:checked');
		var ids = [];
		for (i = 0; i < checkboxes.size(); i++) {
			ids.push(jQuery(checkboxes[i]).attr('name'));
		}

		if (ids.size() == 0) {return false;}

		window.location = exportURL + "?id=" + ids.join(',');
		   
	    jQuery('#cartcount').hide();
	    
		jQuery.ajax({
			url:exportRemoveURL,
			data: {id: ids.join(',')},			
			success: function(response) {
				for(j=0; j<ids.size(); j++){
					jQuery(checkboxes[j]).closest("tr").remove();
					jQuery('#cartcount').show().text(response);
					updateExportCount();
					jQuery('#metadata-viewer').find(".exportaddspan[name='" + ids[j] + "']").addClass("foldericon").addClass("add").addClass("link").text('Add to export');
				}
			},
			error: function(xhr) {
				jQuery('#cartcount').show();
			}
		});
	});

	jQuery('body').on('click', '#closeexport', function() {
		jQuery('#exportOverlay').fadeOut();	
    });
    
   jQuery('body').on('click', '#closefilter', function() {
		jQuery('#filter-browser').fadeOut();	
    });
    
   jQuery('body').on('click', '#closeedit', function() {
		jQuery('#editMetadataOverlay').fadeOut();	
    });

   jQuery('body').on('click', '#closeassay', function() {
		jQuery('#createAssayOverlay').fadeOut();	
   });

   jQuery('body').on('click', '#closeanalysis', function() {
		jQuery('#createAnalysisOverlay').fadeOut();	
  });

   jQuery('body').on('click', '#closefolder', function() {
		jQuery('#createFolderOverlay').fadeOut();	
   });

   jQuery('body').on('click', '#closestudy', function() {
		jQuery('#createStudyOverlay').fadeOut();	
   });
   jQuery('body').on('click', '#closeprogram', function() {
		jQuery('#createProgramOverlay').fadeOut();	
  });

    //Close export and filter overlays on click outside
    jQuery('body').on('click', function(e) {

    	if (!jQuery(e.target).closest('#exportOverlay').length
    	    	&& !jQuery(e.target).closest('#cartbutton').length
    	    	&& jQuery(e.target).attr('id') != 'cartbutton') {
    	
	    	if (jQuery('#exportOverlay').is(':visible')) {
    	    	jQuery('#exportOverlay').fadeOut();
	    	}
    	}
    	
    	if (!jQuery(e.target).closest('#filter-browser').length
    			&& !jQuery(e.target).closest('#filterbutton').length
    	    	&& jQuery(e.target).attr('id') != 'filter-browser') {
    	
	    	if (jQuery('#filter-browser').is(':visible')) {
    	    	jQuery('#filter-browser').fadeOut();
	    	}
    	}
	});

	jQuery('#results-div').on('click', '.result-folder-name', function() {
    	jQuery('.result-folder-name').removeClass('selected');
		jQuery(this).addClass('selected');
    });

    jQuery('#logocutout').on('click', function() {
    	jQuery('#metadata-viewer').empty();

    	jQuery('#welcome-viewer').empty().addClass('ajaxloading');
    	jQuery('#welcome-viewer').load(welcomeURL, {}, function() {
    		jQuery('#welcome-viewer').removeClass('ajaxloading');
    	});
	});

    jQuery('#cartbutton').click(function() {
		jQuery.ajax({
			url:exportViewURL,		
			success: function(response) {
				jQuery('#exportOverlay').html(response);
			},
			error: function(xhr) {
			}
		});
		jQuery('#exportOverlay').fadeToggle();
	});
	
	jQuery('#filterbutton').click(function() {
		jQuery('#filter-browser').fadeToggle();
	});
	
    //addSelectCategories();
    //addFilterCategories();
    //addSearchAutoComplete();
    jQuery(document).ready(window.rwgView.init.bind(window.rwgView));

    
    //Trigger a search immediately if RWG. Dataset Explorer does this on Ext load
    //loadSearchFromSession();
    //if (searchPage == 'RWG') {
		//showSearchResults();
    //}
});

function loadSearchFromSession() {
	var sessionFilters = sessionSearch.split(",,,");
	var sessionOperatorStrings = sessionOperators.split(";");
	
	//This pre-populates the categories array with the search operators - our saved terms will
	//then have the correct operator automatically applied
	for (var i=0; i < sessionOperatorStrings.length; i++) {
		var operatorPair = sessionOperatorStrings[i].split(",");
		var cat = operatorPair[0];
		var op = operatorPair[1];
		
		if (cat != null && cat != "") {
			currentCategories.push(cat);
			currentSearchOperators.push(op);
		}
	}
	
	
	for (var i = 0; i < sessionFilters.length; i++) {
		var item = sessionFilters[i];
		if (item != null && item != "") {
			var itemData = item.split("|");
			var itemSearchData = itemData[1].split(";");
			var searchParam = {id: itemSearchData[2], display: itemData[0], category: itemSearchData[0], keyword: itemSearchData[1]};
			addSearchTerm(searchParam, true, true);
		}
	}
	
	showSearchTemplate();
}

function updateFolder(id) {
	
	var imgExpand = "#imgExpand_"  + id;
	var src = jQuery(imgExpand).attr('src').replace('folderplus.png', 'ajax-loader-flat.gif').replace('folderminus.png', 'ajax-loader-flat.gif');
	jQuery(imgExpand).attr('src',src);
	
	jQuery.ajax({
		url:folderContentsURL,
		data: {id: id, auto: false},
		success: function(response) {
			jQuery('#' + id + '_detail').html(response).addClass('gtb1').addClass('analysesopen').attr('data', true);
			
			//check if the object has children
			if(jQuery('#' + id + '_detail .search-results-table .folderheader').size() > 0){
				jQuery(imgExpand).attr('src', jQuery(imgExpand).attr('src').replace('ajax-loader-flat.gif', 'folderminus.png'));
			}else{
				jQuery(imgExpand).attr('src', jQuery(imgExpand).attr('src').replace('ajax-loader-flat.gif', 'folderleaf.png'));
			}
		},
		error: function(xhr) {
			console.log('Error!  Status = ' + xhr.status + xhr.statusText);
		}
	});
}

function checkSearchLog() {
	
	if (jQuery('#searchlog').size() > 0) {
		jQuery.ajax({
			url:searchLogURL,
			success: function(response) {
				var searchLog = jQuery('#searchlog').empty();
				searchLog.append("<br/>" + "------");
				var log = response.log
				for (var i = 0; i < log.length; i++) {
					searchLog.append("<br/>" + log[i]);
				}
			},
			error: function(xhr) {
				console.log('Error!  Status = ' + xhr.status + xhr.statusText);
			}
		});
	}
}

//go back to the welcome message
function goWelcome() {
	jQuery('#metadata-viewer').empty();
	jQuery('#welcome-viewer').empty().addClass('ajaxloading');
	jQuery('#welcome-viewer').load(welcomeURL, {}, function() {
		jQuery('#welcome-viewer').removeClass('ajaxloading');
	});
}

//display search results numbers
function displayResultsNumber(){
	if(resultNumber!=""){
		var jsonNumbers = JSON.parse(resultNumber);
		
		jQuery('#welcome-viewer').empty();
		jQuery('#metadata-viewer').empty();
		var htmlResults="<div style='margin: 10px;padding: 10px;'><h3 class='rdc-h3'>Search results by type</h3>";
		htmlResults+="<table class='details-table'>";
		htmlResults+="<thead><tr><th class='columnheader'>Object</th><th class='columnheader'>Number of results</th></tr></thead>";
		htmlResults+="<tr class='details-row odd'><td class='columnname'>Programs</td><td class='columnvalue'>"+jsonNumbers.PROGRAM+"</td></tr>";
		htmlResults+="<tr class='details-row odd'><td class='columnname'>Studies</td><td class='columnvalue'>"+jsonNumbers.STUDY+"</td></tr>";
		htmlResults+="<tr class='details-row odd'><td class='columnname'>Assays</td><td class='columnvalue'>"+jsonNumbers.ASSAY+"</td></tr>";
		htmlResults+="<tr class='details-row odd'><td class='columnname'>Analyses</td><td class='columnvalue'>"+jsonNumbers.ANALYSIS+"</td></tr>";
		htmlResults+="<tr class='details-row odd'><td class='columnname'>Folders</td><td class='columnvalue'>"+jsonNumbers.FOLDER+"</td></tr>";
		htmlResults+="</table></div>";
		jQuery('#metadata-viewer').html(htmlResults);
	}
}

//Globally prevent AJAX from being cached (mostly by IE)
jQuery.ajaxSetup({
	cache: false
});

