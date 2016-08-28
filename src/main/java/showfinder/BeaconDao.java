package showfinder;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;

import java.util.List;

/**
 * Contains the methods to interact with the persistence layer for ScoreKeeper in DynamoDB.
 */
public class BeaconDao {
    private final BeaconDynamoDbClient dynamoDBClient;

    public BeaconDao(BeaconDynamoDbClient dynamoDBClient) {
        this.dynamoDBClient = dynamoDBClient;
    }

    public BeaconUserDataItem getBeaconUserDataItem(String itemName) {
        BeaconUserDataItem item = new BeaconUserDataItem();
        item.setBeaconCategory(itemName);

        item = dynamoDBClient.loadItem(item);

        return item;

    }

    public String getBeaconDataByCategory(String beaconCategory) {
        BeaconUserDataItem item = getBeaconUserDataItem(beaconCategory);

        return item.getStore();

/*        DynamoDBQueryExpression<BeaconUserDataItem> queryExpression = new DynamoDBQueryExpression<BeaconUserDataItem>()
                .withHashKeyValues(item);

        List<BeaconUserDataItem> itemList = dynamoDBClient.createDynamoDBMapper().
                query(BeaconUserDataItem.class, queryExpression);

        for (int i = 0; i < itemList.size(); i++) {
            store = itemList.get(i).getStore();
        }*/
    }
}
