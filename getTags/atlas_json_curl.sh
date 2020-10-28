#!/bin/bash
if [[ $# -lt 3 ]]; then
    echo "$0 {User Name} {Password} {Zip File Name}"
    exit 0
fi

curl -v -u $1:$2 -H "Content-Type: application/json" "https://azalvedlmstdp07.p01eaedl.manulife.com:21443/api/atlas/v2/search/basic?limit=5000&classification=CAS" -o cas.json
curl -v -u $1:$2 -H "Content-Type: application/json" "https://azalvedlmstdp07.p01eaedl.manulife.com:21443/api/atlas/v2/search/basic?limit=5000&classification=VN" -o VN.json
curl -v -u $1:$2 -H "Content-Type: application/json" "https://azalvedlmstdp07.p01eaedl.manulife.com:21443/api/atlas/v2/search/basic?limit=5000&classification=Regulated_PHI" -o Regulated_PHI.json
curl -v -u $1:$2 -H "Content-Type: application/json" "https://azalvedlmstdp07.p01eaedl.manulife.com:21443/api/atlas/v2/search/basic?limit=5000&classification=Regulated_PIFI" -o Regulated_PIFI.json
curl -v -u $1:$2 -H "Content-Type: application/json" "https://azalvedlmstdp07.p01eaedl.manulife.com:21443/api/atlas/v2/search/basic?limit=5000&classification=Regulated_PII" -o Regulated_PII.json
curl -v -u $1:$2 -H "Content-Type: application/json" "https://azalvedlmstdp07.p01eaedl.manulife.com:21443/api/atlas/v2/search/basic?limit=5000&classification=Customer" -o Customer.json
curl -v -u $1:$2 -H "Content-Type: application/json" "https://azalvedlmstdp07.p01eaedl.manulife.com:21443/api/atlas/v2/search/basic?limit=5000&classification=Restricted" -o Restricted.json
curl -v -u $1:$2 -H "Content-Type: application/json" "https://azalvedlmstdp07.p01eaedl.manulife.com:21443/api/atlas/v2/search/basic?limit=5000&classification=Finance" -o Finance.json
curl -v -u $1:$2 -H "Content-Type: application/json" "https://azalvedlmstdp07.p01eaedl.manulife.com:21443/api/atlas/v2/search/basic?limit=5000&classification=Internal" -o Internal.json
curl -v -u $1:$2 -H "Content-Type: application/json" "https://azalvedlmstdp07.p01eaedl.manulife.com:21443/api/atlas/v2/search/basic?limit=5000&classification=AMS" -o AMS.json
curl -v -u $1:$2 -H "Content-Type: application/json" "https://azalvedlmstdp07.p01eaedl.manulife.com:21443/api/atlas/v2/search/basic?limit=5000&classification=Product" -o Product.json
curl -v -u $1:$2 -H "Content-Type: application/json" "https://azalvedlmstdp07.p01eaedl.manulife.com:21443/api/atlas/v2/search/basic?limit=5000&classification=VNDM" -o VNDM.json
curl -v -u $1:$2 -H "Content-Type: application/json" "https://azalvedlmstdp07.p01eaedl.manulife.com:21443/api/atlas/v2/search/basic?limit=5000&classification=Public" -o Public.json
curl -v -u $1:$2 -H "Content-Type: application/json" "https://azalvedlmstdp07.p01eaedl.manulife.com:21443/api/atlas/v2/search/basic?limit=5000&classification=Distributor" -o Distributor.json
curl -v -u $1:$2 -H "Content-Type: application/json" "https://azalvedlmstdp07.p01eaedl.manulife.com:21443/api/atlas/v2/search/basic?limit=5000&classification=Employee" -o Employee.json
curl -v -u $1:$2 -H "Content-Type: application/json" "https://azalvedlmstdp07.p01eaedl.manulife.com:21443/api/atlas/v2/search/basic?limit=5000&classification=Actuary" -o Actuary.json
java -jar json2csv.jar Regulated_PHI.json Regulated_PHI.csv
java -jar json2csv.jar Regulated_PII.json Regulated_PII.csv
java -jar json2csv.jar Regulated_PIFI.json Regulated_PIFI.csv
java -jar json2csv.jar Customer.json Customer.csv
java -jar json2csv.jar Restricted.json Restricted.csv
java -jar json2csv.jar Finance.json Finance.csv
java -jar json2csv.jar Internal.json Internal.csv
java -jar json2csv.jar AMS.json AMS.csv
java -jar json2csv.jar Product.json Product.csv
java -jar json2csv.jar VNDM.json VNDM.csv
java -jar json2csv.jar Public.json Public.csv
java -jar json2csv.jar Distributor.json Distributor.csv
java -jar json2csv.jar Employee.json Employee.csv
java -jar json2csv.jar CAS.json CAS.csv
java -jar json2csv.jar VN.json VN.csv
java -jar json2csv.jar Actuary.json Actuary.csv
rm $3.zip
zip $3.zip Actuary.csv VN.csv CAS.csv Regulated_PHI.csv Regulated_PII.csv Regulated_PIFI.csv Customer.csv Restricted.csv Finance.csv Internal.csv AMS.csv Product.csv VNDM.csv VNDM.csv Public.csv Distributor.csv Employee.csv
