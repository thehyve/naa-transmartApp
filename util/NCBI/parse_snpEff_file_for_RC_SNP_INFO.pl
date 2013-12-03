#!/usr/bin/perl -w

## This perl script was developed to parse SNP VCF data from NCBI and generate proper output to be loaded into DE_RC_SNP_INFO table
## It takes several input files as outlined in the README.txt file
## It will also integrate the regulome score and recombination rates.
### If you don't have the exact directory structure, you may have to change a few lines below.

use strict;

## Declare variables
our $in = "00-All.vcf.snpEff";
# our $db_service = "tsmrt03";

our ($gene_name,$geneid,$gene_info,%entrezlist,$chr,$pos,$rs,$info,$regulome_score,%regulome);
our ($i,@chroms,$recombination_rate,$recombination_map,$position,%rate,%map);
our ($maf,$vc,$sao,$clnsigW,$disease,@t,$ref,$alt,$strand,%regular_part);
our ($snpeff,@effects,$previous_score,@id_list,$effect,$effect_detail,$score,%final_detail);
our ($dir,$impact,$functional_class,$codon_change,$amino_acid_change,$amino_acid_length,$gene_biotype,$gene_coding);
our ($transcript_id,$exon_id,$genotypeNum);

our $hg_version = "19";
our $out = $in . ".de_rc_snp_info";
our $control = "load_de_rc_snp_info.ctl";
our $shellscript = "load_de_rc_snp_info.sh";

## Set the priority order for the RS_ID if they were assigned to the same gene
our %effect_score =( 
	'DOWNSTREAM' => -1,
	'UPSTREAM' => -1,
	'INTRON' => 0,
	'SPLICE_SITE_ACCEPTOR' => 1,
	'SPLICE_SITE_DONOR' => 1,
	'EXON' => 2,
	'UTR_5_PRIME' => 2,
	'UTR_3_PRIME' => 2,
	'UTR_5_DELETED' => 3,
	'SYNONYMOUS_CODING' => 3,
	'SYNONYMOUS_START' => 3,
	'SYNONYMOUS_STOP' => 3,
	'NON_SYNONYMOUS_CODING' => 4,
	'CODON_INSERTION' => 5,
	'CODON_DELETION' => 5,
	'CODON_CHANGE_PLUS_CODON_INSERTION' => 5,
	'CODON_CHANGE_PLUS_CODON_DELETION' => 5,
	'NON_SYNONYMOUS_START' => 5,
	'RARE_AMINO_ACID' => 5,
	'FRAME_SHIFT' => 6,
	'EXON_DELETED' => 6,
	'START_GAINED' => 6,
	'START_LOST' => 6,
	'STOP_GAINED' => 6,
	'STOP_LOST' => 6
);

## Save the relationship between RS ID and Entrez gene ID
open ENTREZ, "< genelist_b137_human_9606.txt" or die "Cannot open file:$!";
while (<ENTREZ>) {
chomp;
	if (/chr\w+\_(\w+)\_(\d+)\.xml\.gz/) {

		$gene_name = $1;
		$geneid = $2;
		$entrezlist{$gene_name} = $geneid;
        } elsif (/chr\w+\_(\w+\-\w+)\_(\d+)\.xml\.gz/) {
		$gene_name = $1;
                $geneid = $2;
                $entrezlist{$gene_name} = $geneid;
	}
}
close ENTREZ;

## Save the Regulome score
$dir = "../regulome_score";
for ($i = 1; $i < 8; $i++)  {
	open REG, "< $dir/RegulomeDB.dbSNP132.Category${i}.txt" or die  "Cannot open file:$!";
	while (<REG>) {
	chomp;
		($chr,$pos,$rs,$info,$regulome_score) = split(/\t/);
		$regulome{$rs} = $regulome_score;
	}
	close REG;
}

## Save the Recombination Map and Rate info
$dir = "../recombination_rates";
for ($i = 1; $i < 23; $i++) {
	push @chroms, $i;
}
push @chroms, "X";
push @chroms, "X_par1";
push @chroms, "X_par2";

for $i (@chroms) {
	open MAP, "< $dir/genetic_map_GRCh37_chr${i}.txt" or die "Cannot open file:$!";
	while (<MAP>) {
	chomp;
		next if /^Chromosome/;
		($chr,$pos,$recombination_rate,$recombination_map) = split(/\t/);
		$chr =~ s/chr//;
		$chr =~ s/_par1//;
		$chr =~ s/_par2//;

		$position = "$chr:$pos";
		$rate{$position} = $recombination_rate;
		$map{$position} = $recombination_map;
	}
	close MAP;
}


## Now start to process the SNP VCF file (pre-processed by snpEff program)
open IN, "< $in" or die "Cannot open file:$!";
open OUT, "> $out" or die "Cannot open file:$!";

while (<IN>) {
chomp;
        next if /^\#/;
	next if /ERROR_CHROMOSOME_NOT_FOUND/;

        @t = split(/\t/);

        $chr = $t[0];
        $pos = $t[1];
        $rs = $t[2];
        $ref = $t[3];
        $alt = $t[4];
        $info = $t[7];

	## We set the maximum length of each SNP to 1000 in the DE_RC_SNP_INFO table
	## If you want to change it, you must modify the table column
	next if (length($ref) > 1000 or length($alt) > 1000);

	# GMAF has been changed to CAF in the new file - as October 2013
        if ($info =~ /CAF=\[\d+\,(\d+)\]\;/) {
                $maf = $1;
        } else {
		$maf = "";
	}

        if ($info =~ /VC=(\w+)\;/) {
                $vc = $1;
        } else {
		$vc = "";
	}

	if ($info =~ /\;RV\;/) {
		$strand = "-";
	} else {
		$strand = "+";
	}

	## Remove intergenic (non-gene) RS_IDs (> 50%)
	next if ($info =~ /INTERGENIC/);  # This has been tested that these RS_IDs are not within EXON or INTRON regions

	$regular_part{$rs} = "$chr|$pos|$ref|$alt|$maf|$vc|$strand";

	if ($info =~ /EFF\=(.*)/) {
		## Some EFF have multiple entries and they are separated by ","
		$snpeff = $1;
		@effects = split (/\,/, $snpeff);

		$previous_score = "-10";
		push @id_list, $rs;

		for ($i = 0; $i <= $#effects; $i++) {  ## Loop through each mapped gene
			if ($effects[$i] =~ /(\w+)\((.*)\)/) {
				$effect = $1;
				$effect_detail = $2;
				if (defined $effect_score{$effect}) {
				  $score = $effect_score{$effect};
				} else {
#					print "Score not found for $effect\n";
					print "Please set a score for $effect at the beginning and re-run this script.\n";
#					exit;
				}
				  if ($score > $previous_score) { ## Only keep the highest effect per RS ID ******
                               		$final_detail{$rs} = "$effect|$effect_detail";
                               		$previous_score = $score;
                       		  }
			}
		}
	}	
	valueReset();	
}
close IN;

## print the output
for $rs (@id_list) {
	($chr,$pos,$ref,$alt,$maf,$vc,$strand) = split(/\|/, $regular_part{$rs});
	($effect,$impact,$functional_class,$codon_change,$amino_acid_change,$amino_acid_length,$gene_name,$gene_biotype,$gene_coding,$transcript_id,$exon_id,$genotypeNum) = split(/\|/,$final_detail{$rs});
	if ($gene_name eq "") {
		$geneid = "";
                $gene_info = "";
		print "Gene name is empty for $rs\n";
	} else {
	  if (defined $entrezlist{$gene_name}) {
		$geneid = $entrezlist{$gene_name};
		$gene_info = $gene_name.":".$geneid;
	  } else {
		$geneid = "";
		$gene_info = "";
	  }
	}
	if (defined $regulome{$rs}) {
		$regulome_score = $regulome{$rs};
	} else {
		$regulome_score = "";
	}
	$position = "$chr:$pos";
	if (defined $rate{$position}) {
		$recombination_rate = $rate{$position};
		$recombination_map = $map{$position};
	} else {
		$recombination_rate = "";
		$recombination_map = "";
	}

	print OUT join("\t",$rs,$chr,$pos,$ref,$alt,$gene_info,$vc,$strand,$maf,$gene_biotype,$impact,$transcript_id,$functional_class,$effect,$exon_id,$amino_acid_change,$codon_change,$hg_version,$gene_name,$geneid,$recombination_rate,$recombination_map,$regulome_score),"\n";

}
close OUT;
 
exit;

## The following can be used if you want to load the de_rc_snp_info table from scratch  *******

## write a control file for SQL loader

open CTL, "> $control" or die "Cannot open file:$!";
print CTL "
INFILE '$out'
   INTO TABLE DE_RC_SNP_INFO
   FIELDS TERMINATED BY X'9'
   TRAILING NULLCOLS
   (
    RS_ID,
    CHROM,
    POS,
    REF,
    ALT,
    GENE_INFO,
    VARIATION_CLASS,
    STRAND,
    GMAF,
    GENE_BIOTYPE,
    IMPACT,
    TRANSCRIPT_ID,
    FUNCTIONAL_CLASS,
    EFFECT,
    EXON_ID,
    AMINO_ACID_CHANGE,
    CODON_CHANGE,
    HG_VERSION,
    GENE_NAME,
    ENTREZ_ID,
    RECOMBINATION_RATE,
    REGULOME_SCORE,
    SNP_INFO_ID \"DE_RC_SNP_INFO_SEQ.nextval\"
    )
";
close CTL;

## Print the shell script ready to be run
open SH, "> $shellscript" or die "Cannot open file:$!";
print SH "
sqlldr deapp/deapp\@$db_service control=load_de_rc_snp_info.ctl ROWS=2000 errors=200000\n\n";
close SH;
system "chmod 755 $shellscript";

sub valueReset {
	$chr = "";
	$pos = "";
	$rs = "";
	$ref = "";
	$alt = "";
	$info = "";
	$strand = "";
	$maf = "";
        $gene_name = "";
        $vc = "";
}

my $note = "
Some data in the current database are pretty old but they may be needed.
For example, rs78643169 on chr21.  The new rs id is rs149536598 at position chr21:15013735
";

