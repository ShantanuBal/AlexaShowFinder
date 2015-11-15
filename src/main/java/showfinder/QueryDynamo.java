package showfinder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.util.Tables;
public class QueryDynamo {
	String credential_path;
	static AmazonDynamoDBClient dynamoDB;
	QueryDynamo(String credential_path) throws Exception{
		this.credential_path = credential_path;
		init();
	}
    public void init() throws Exception {
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider(this.credential_path,"default").getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        dynamoDB = new AmazonDynamoDBClient(credentials);
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        dynamoDB.setRegion(usWest2);
    }
    public String getUserAccessCode(String user_id){
    	String result = "Error Occured";
    	try{
    		String tableName = "user_access_code";
    		HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
    		Condition condition = new Condition()
            .withComparisonOperator(ComparisonOperator.EQ.toString())
            .withAttributeValueList(new AttributeValue().withS(user_id));
    		scanFilter.put("user_id", condition);
    		ScanRequest scanRequest = new ScanRequest(tableName).withScanFilter(scanFilter);
    		ScanResult scanResult = dynamoDB.scan(scanRequest);
    		List<Map<String,AttributeValue>> items = scanResult.getItems();
    		Map<String,AttributeValue> uid_key_value = items.get(0);
    		AttributeValue uid = uid_key_value.get("user_id");
    		AttributeValue access_code = uid_key_value.get("access_code");
    		result = access_code.getS();
    		return result;
    	}catch(Exception e){
    		return result;
    	}
    }
	
}
