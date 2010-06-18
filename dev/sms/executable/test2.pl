
print "2. Clean database first...\n";
print " (0. both, 1: rawdb, 2: mdb)\n\n";

print "Load synthetic data .............\n";
print "need to run sync_data_generator.sh first\n";

$cmd = "java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader ";
$dbname = " oboe_syn";

$num2 = 400;
print "0.2 ==> filenum: " . $num2 ."\n";
for ($d = 1; $d <= $num2; $d++) {
	$datafile = "synchain_0.2-n5000-d" . $d ."-data.txt";
	
	$curcmd = $cmd . $datafile . $dbname;
	if($d % 200==0){	
		print "$curcmd\n";
	}
	//system $curcmd;
}

print "\n";
