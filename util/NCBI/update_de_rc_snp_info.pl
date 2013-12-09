#!/usr/bin/perl -w

use strict;

## Declare variables
our $in = "00-All.vcf.snpEff.de_rc_snp_info";

our ($gene_name,$geneid,$gene_info,%entrezlist,$chr,$pos,$rs,$info,$regulome_score,%regulome);
our ($i,@chroms,$recombination_rate,$recombination_map,$position,%rate,%map);
our ($maf,$vc,$sao,$clnsigW,$disease,@t,$ref,$alt,$strand,%regular_part);
our ($snpeff,@effects,$previous_score,@id_list,$effect,$effect_detail,$score,%final_detail);
our ($dir,$impact,$functional_class,$codon_change,$amino_acid_change,$amino_acid_length,$gene_biotype,$gene_coding);
our ($transcript_id,$exon_id,$genotypeNum);
our (%positions,%ie,$flag,$new_rs);

our $hg_version ;
our $out = "de_rc_snp_info_update.sql";

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


open IN, "< $in" or die "Cannot open file:$!";
while (<IN>) {
chomp;
	($rs,$chr,$pos,$ref,$alt,$gene_info,$vc,$strand,$maf,$gene_biotype,$impact,$transcript_id,$functional_class,$effect,$exon_id,$amino_acid_change,$codon_change,$hg_version,$gene_name,$geneid,$recombination_rate,$recombination_map,$regulome_score) = split(/\t/);
	$position = "$chr:$pos";
	$rate{$rs} = $recombination_rate;
	$map{$rs} = $recombination_map;
	$regulome{$rs} = $regulome_score;
	$positions{$position} = $rs;

	if ($effect_score{$effect} < 2) {
		$flag = "intron";  # intron
	} else {
		$flag = "exon";
	}
	$ie{$rs} = $flag;
}
close IN;

my $count = 1;
open IN, "< orig_de_rc_snp_info.txt" or die "Cannot open file:$!";
open OUT, "> $out" or die "Cannot open file:$!";
open TEMP, "> tempfile";
while (<IN>) {
chomp;
	($rs,$chr,$pos,$hg_version) = split;
	$position = "$chr:$pos";

	if (defined $rate{$rs}) {
		$count++;
		print OUT "update de_rc_snp_info set exon_intron = '$ie{$rs}', recombination_rate = '$rate{$rs}', recombination_map = '$map{$rs}', regulome_score = '$regulome{$rs}' where rs_id = '$rs';\n";
	} elsif (defined $positions{$position}) { # These are the merged RS_IDs, no longer exist in dbSNP but Pfizer still keeps the old data, so we just update them based on the posiitons
		$count++;
		$new_rs = $positions{$position};
		print OUT "update de_rc_snp_info set exon_intron = '$ie{$new_rs}', recombination_rate = '$rate{$new_rs}', recombination_map = '$map{$new_rs}', regulome_score = '$regulome{$new_rs}' where rs_id = '$rs';\n";
	} else {  # most likely they are intergenic
		print TEMP "$rs\t$chr\t$pos\n";
	}

	if ($count % 2000 == 0) {
		print OUT "commit;\n";
	}
}
print OUT "commit;\nexit;\n";	
close IN;
close OUT;
close TEMP;

