# Curl Command for Atlas Rest API

## Search Table + Columns
```
curl -v -u holger_gov:holger_gov 'http://sandbox.hortonworks.com:21000/api/atlas/v2/search/dsl?limit=25&query=where+qualifiedName+%3D%22default.employee%40Sandbox%22+%2C+columns&typeName=hive_table' -H 'Accept: application/json'
```

## Search Table
```
curl -v -u holger_gov:holger_gov 'http://sandbox.hortonworks.com:21000/api/atlas/v2/search/dsl?limit=25&query=where+qualifiedName+%3D%22default.employee%40Sandbox%22&typeName=hive_table' -H 'Accept: application/json'
```

## Tag Create
```
curl -v -u holger_gov:holger_gov 'http://sandbox.hortonworks.com:21000/api/atlas/v2/types/typedefs?type=classification' -H 'Content-Type: application/json' -H 'Accept: application/json' --data-binary '{"classificationDefs":[{"name":"Test3","description":"","superTypes":[],"attributeDefs":[]}],"entityDefs":[],"enumDefs":[],"structDefs":[]}'
```

## Tag assignment Col 
```
curl 'http://sandbox.hortonworks.com:21000/api/atlas/v2/entity/bulk/classification' -H 'Content-Type: application/json' -H 'Accept: application/json' --data-binary '{"classification":{"typeName":"Test1","attributes":{}},"entityGuids":["258e2aaa-7edd-4676-9b4e-a95cec29ac07"]}'
```

## Tag Assigment Table
```
curl 'http://sandbox.hortonworks.com:21000/api/atlas/v2/entity/bulk/classification' -H 'Content-Type: application/json'  -H 'Accept: application/json' --data-binary '{"classification":{"typeName":"Test1","attributes":{}},"entityGuids":["9cff3c74-bcd5-4bba-97a7-a7d2f87ed617"]}'
```