
print "\nLoad synthetic data .............\n";

print "4. Materialize synthatic data.........\n";

################## start time
$start = time;

$cmd = "java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.MaterializeDB null ";
$dbname = " oboe_syn";

$num001 = 2;
print "0.001 ==> filenum: " . $num001 ."\n";
$annotfile001 = "synchain_0.001-annot.xml ";
for ($d = 1; $d <= $num001; $d++) {
	$datafile = "synchain_0.001-n5000-d" . $d ."-data.txt ";
	$outputPrefix = "synchain_0.001-n5000-d" . $d;
	
	$curcmd = $cmd . $annotfile001 . $datafile . $outputPrefix . $dbname;	
	print "$curcmd\n";
}

$num01 = 20;
$annotfile01 = "synchain_0.01-annot.xml ";
print "0.01 ==> filenum: " . $num01 ."\n";
for ($d = 1; $d <= $num01; $d++) {
	$datafile = "synchain_0.01-n5000-d" . $d ."-data.txt ";
	$outputPrefix = "synchain_0.01-n5000-d" . $d;
	
	$curcmd = $cmd . $annotfile01 . $datafile . $outputPrefix . $dbname;		
	if($d % 10==0){	
		print "$curcmd\n";
	}
}

$start_05 = time;
$num05 = 100;
$annotfile05 = "synchain_0.05-annot.xml ";
print "0.05 ==> filenum: " . $num05 ."\n";
for ($d = 1; $d <= $num05; $d++) {
	$datafile = "synchain_0.05-n5000-d" . $d ."-data.txt ";
	$outputPrefix = "synchain_0.05-n5000-d" . $d;
	
	$curcmd = $cmd . $annotfile05 . $datafile . $outputPrefix . $dbname;	
	if($d % 50==0){	
		print "$curcmd\n";
	}
}
$end_05 = time;
print "0.05 Time elapsed = " . ($end05-$start05) ." ***\n"; 


$num1 = 200;
$annotfile1 = "synchain_0.1-annot.xml ";
print "0.1 ==> filenum: " . $num1 ."\n";
for ($d = 1; $d <= $num1; $d++) {
	$datafile = "synchain_0.1-n5000-d" . $d ."-data.txt ";
	$outputPrefix = "synchain_0.1-n5000-d" . $d;
	
	$curcmd = $cmd . $annotfile1 . $datafile . $outputPrefix . $dbname;
	if($d % 100==0){	
		print "$curcmd\n";
	}
}

$num2 = 400;
$annotfile2 = "synchain_0.2-annot.xml ";
print "0.2 ==> filenum: " . $num2 ."\n";
for ($d = 1; $d <= $num2; $d++) {
	$datafile = "synchain_0.2-n5000-d" . $d ."-data.txt ";
	$outputPrefix = "synchain_0.2-n5000-d" . $d;
	
	$curcmd = $cmd . $annotfile2 . $datafile . $outputPrefix . $dbname;
	if($d % 200==0){	
		print "$curcmd\n";
	}
}

$num5 = 1000;
$annotfile5 = "synchain_other-annot.xml ";
print "0.5 ==> filenum: " . $num5 ."\n";
for ($d = 1; $d <= $num5; $d++) {
	$datafile = "synchain_0.5-n5000-d" . $d ."-data.txt ";
	$outputPrefix = "synchain_0.5-n5000-d" . $d;
	
	$curcmd = $cmd . $annotfil52 . $datafile . $outputPrefix . $dbname;
	if($d % 500==0){	
		print "$curcmd\n";
	}
}

$num_other = 278;
$annotfile_other = "synchain_0.5-annot.xml ";
print "0.5 ==> filenum: " . $num_other ."\n";
for ($d = 1; $d <= $num_other; $d++) {
	$datafile = "synchain_other-n5000-d" . $d ."-data.txt";
	$outputPrefix = "synchain_other-n5000-d" . $d;
	
	$curcmd = $cmd . $annotfil_other . $datafile . $outputPrefix . $dbname;
	if($d % 100==0){	
		print "$curcmd\n";
	}
}
print "\n";

$end = time;
print "*** Total Time elapsed = " . ($end-$start) ." \n";