
print "2. Clean database first...\n";
print " (0. both, 1: rawdb, 2: mdb)\n\n";

print "Load synthetic data .............\n";
print "need to run sync_data_generator.sh first\n";

$cmd = "java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader ";
$dbname = " oboe_syn";

$num001 = 2;
print "0.001 ==> filenum: " . $num001 ."\n";
for ($d = 1; $d <= $num001; $d++) {
	$datafile = "synchain_0.001-n5000-d" . $d ."-data.txt";
	$curcmd = $cmd . $datafile . $dbname;	
	print "$curcmd\n";
	
	system $curcmd;
}

$num01 = 20;
print "0.01 ==> filenum: " . $num01 ."\n";
for ($d = 1; $d <= $num01; $d++) {
	$datafile = "synchain_0.01-n5000-d" . $d ."-data.txt";
	$curcmd = $cmd . $datafile . $dbname;	
	if($d % 10==0){	
		print "$curcmd\n";
	}
	system $curcmd;
}

$num05 = 100;
print "0.05 ==> filenum: " . $num05 ."\n";
for ($d = 1; $d <= $num05; $d++) {
	$datafile = "synchain_0.05-n5000-d" . $d ."-data.txt";
	$curcmd = $cmd . $datafile . $dbname;	
	if($d % 50==0){	
		print "$curcmd\n";
	}
	system $curcmd;
}

$num1 = 200;
print "0.1 ==> filenum: " . $num1 ."\n";
for ($d = 1; $d <= $num1; $d++) {
	$datafile = "synchain_0.1-n5000-d" . $d ."-data.txt";
	$curcmd = $cmd . $datafile . $dbname;
	if($d % 100==0){	
		print "$curcmd\n";
	}
	system $curcmd;
}

$num2 = 400;
print "0.2 ==> filenum: " . $num2 ."\n";
for ($d = 1; $d <= $num2; $d++) {
	$datafile = "synchain_0.2-n5000-d" . $d ."-data.txt";
	
	$curcmd = $cmd . $datafile . $dbname;
	if($d % 200==0){	
		print "$curcmd\n";
	}
	system $curcmd;
}

$num5 = 1000;
print "0.5 ==> filenum: " . $num5 ."\n";
for ($d = 1; $d <= $num5; $d++) {
	$datafile = "synchain_0.5-n5000-d" . $d ."-data.txt";
	$curcmd = $cmd . $datafile . $dbname;
	if($d % 500==0){	
		print "$curcmd\n";
	}
	system $curcmd;
}

$num_other = 278;
print "0.5 ==> filenum: " . $num_other ."\n";
for ($d = 1; $d <= $num_other; $d++) {
	$datafile = "synchain_other-n5000-d" . $d ."-data.txt";
	$curcmd = $cmd . $datafile . $dbname;
	if($d % 100==0){	
		print "$curcmd\n";
	}
	system $curcmd;
}
print "\n";
