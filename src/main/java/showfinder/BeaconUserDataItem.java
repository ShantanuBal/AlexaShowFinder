package showfinder;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshalling;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Model representing an item of the ScoreKeeperUserData table in DynamoDB for the ScoreKeeper
 * skill.
 */
@DynamoDBTable(tableName = "EstimoteBeacon")
public class BeaconUserDataItem {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String beaconCategory;
    private String store;

    @DynamoDBHashKey(attributeName = "BeaconCategory")
    public String getBeaconCategory() {
        return beaconCategory;
    }
    public void setBeaconCategory(String beaconCategory) {
        this.beaconCategory = beaconCategory;
    }

    @DynamoDBAttribute(attributeName = "Store")
    public String getStore() {
        return store;
    }
    public void setStore(String store) {
        this.store = store;
    }
}