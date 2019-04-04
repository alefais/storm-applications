package Constants;

/**
 * Constants peculiar of the TrafficMonitoring application.
 */
public interface TrafficMonitoringConstants extends BaseConstants {
    String DEFAULT_PROPERTIES = "/trafficmonitoring/tm.properties";
    String DEFAULT_TOPO_NAME = "TrafficMonitoring";
    String BEIJING_SHAPEFILE = "../../data/app/tm/beijing/roads.shp";
    String DUBLIN_SHAPEFILE = "../../data/app/tm/dublin/roads.shp";

    interface Conf {
        String MAP_MATCHER_SHAPEFILE = "tm.map_matcher.shapefile";
        String MAP_MATCHER_LAT_MIN = "tm.map_matcher.lat.min";
        String MAP_MATCHER_LAT_MAX = "tm.map_matcher.lat.max";
        String MAP_MATCHER_LON_MIN = "tm.map_matcher.lon.min";
        String MAP_MATCHER_LON_MAX = "tm.map_matcher.lon.max";
        
        String ROAD_FEATURE_ID_KEY    = "tm.road.feature.id_key";
        String ROAD_FEATURE_WIDTH_KEY = "tm.road.feature.width_key";

        String MAP_MATCHER_THREADS = "tm.map_matcher.threads";
        String SPEED_CALCULATOR_THREADS = "tm.speed_calculator.threads";
    }
    
    interface Component extends BaseComponent {
        String MAP_MATCHER = "map_matcher_bolt";
        String SPEED_CALCULATOR = "speed_calculator_bolt";
    }
    
    interface Field {
        String TIMESTAMP = "timestamp";
        String VEHICLE_ID = "vehicleID";
        String DATE_TIME = "dateTime";
        String OCCUPIED = "occupied";
        String SPEED = "speed";
        String BEARING = "bearing";
        String LATITUDE = "latitude";
        String LONGITUDE = "longitude";
        String ROAD_ID = "roadID";
        String NOW_DAT = "nowDate";
        String AVG_SPEED = "averageSpeed";
        String COUNT = "count";
    }

    interface City {
        String BEIJING =
    }
}
