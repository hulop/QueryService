pwd=`pwd`

mkdir -p ./QueryService/WebContent/js/lib/
cd ./QueryService/WebContent/js/lib/

jquery=jquery

mkdir $jquery
cd $jquery
curl -O https://code.jquery.com/jquery-1.11.2.js
curl -O https://code.jquery.com/jquery-1.11.2.min.js

cd ..

curl -L -O https://jqueryui.com/resources/download/jquery-ui-1.11.4.zip
unzip jquery-ui-1.11.4.zip

curl -L -O https://github.com/DataTables/DataTables/archive/1.10.6.zip
unzip 1.10.6.zip

cd $pwd
mkdir -p ./QueryService/WebContent/WEB-INF/lib
cd ./QueryService/WebContent/WEB-INF/lib

curl -L -O http://central.maven.org/maven2/org/mongodb/mongo-java-driver/3.4.0/mongo-java-driver-3.4.0.jar

curl -L -O http://archive.apache.org/dist/wink/1.4.0/apache-wink-1.4.zip
unzip apache-wink-1.4.zip apache-wink-1.4/dist/wink-1.4.jar
mv apache-wink-1.4/dist/wink-1.4.jar ./
