## Disk Based Key-Value storage (Experimental)

### Features
* writes are constant time and reads are ln(n) time
* uses WAL for crash recovery
* key is a string and value is JSONObject
* data is sorted by key
* operations available : put,get,remove (update can done using put)
* put accepts optional time to live value

### How to use
```java
BucketDB bucketDB=BucketDB.getInstance(dbPath);

JSONObject jsonObject= new JSONObject();
jsonObject.put("name","icircuit");
jsonObject.put("address","chennai");
bucketDB.put("key1",jsonObject);
bucketDB.get("key2");
bucketDB.remove("key2");
```
### How to run tests
`mvn test`