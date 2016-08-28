package showfinder;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;

import java.util.List;
import java.util.Map;

/**
 * Contains the methods to interact with the persistence layer for ScoreKeeper in DynamoDB.
 */
public class BeaconDao {
    private final BeaconDynamoDbClient dynamoDBClient;
    private BeaconUserDataItem beaconUserDataItem;

    public BeaconUserDataItem getBeaconUserDataItem(String itemKey) {
        if (beaconUserDataItem == null) {
            beaconUserDataItem = new BeaconUserDataItem();
            beaconUserDataItem.setBeaconCategory(itemKey);

            beaconUserDataItem = dynamoDBClient.loadItem(beaconUserDataItem);
        }
        return beaconUserDataItem;
    }

    public BeaconDao(BeaconDynamoDbClient dynamoDBClient) {
        this.dynamoDBClient = dynamoDBClient;
    }

    public List<Map<String, String>> getBeaconValue(String beaconCategory) {
        BeaconUserDataItem item = getBeaconUserDataItem(beaconCategory);
        return item.getBeaconValue();
    }
}
