
echo "============================="
echo "1. Load real data"
echo "12 attributes, 21373 rows"
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader mobrien.36.59.stream_chemistry_allyears.txt oboe_real
echo "4 attributes, 51531 rows"
java -cp oboe.jar org.ecoinformatics.oboe.RawDataLoader mobrien.38.19.AB00_allyears.txt oboe_real

echo "============================="
echo "2. Materialize real data from Margarat"
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.MaterializeDB null mobrien.36.59.xml mobrien.36.59.stream_chemistry_allyears.txt mobrien.36.59 >resultlog/mobrien.36.59.mat.txt
java -cp oboe.jar -Xmx4096m org.ecoinformatics.oboe.MaterializeDB null mobrien.38.19.xml mobrien.38.19.AB00_allyears.txt mobrien.38.19 >resultlog/mobrien.38.19.mat.txt
