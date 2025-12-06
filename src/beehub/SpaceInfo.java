package beehub;

public class SpaceInfo {
    private int spaceId;
    private String buildingName;
    private String roomName;
    private int minPeople;
    private int maxPeople;
    private String operTime;
    private String roomType;
    private boolean active;

    public SpaceInfo(int spaceId, String buildingName, String roomName,
                     int minPeople, int maxPeople, String operTime,
                     String roomType, boolean active) {
        this.spaceId = spaceId;
        this.buildingName = buildingName;
        this.roomName = roomName;
        this.minPeople = minPeople;
        this.maxPeople = maxPeople;
        this.operTime = operTime;
        this.roomType = roomType;
        this.active = active;
    }

    public int getSpaceId() { return spaceId; }
    public String getBuildingName() { return buildingName; }
    public String getRoomName() { return roomName; }
    public int getMinPeople() { return minPeople; }
    public int getMaxPeople() { return maxPeople; }
    public String getOperTime() { return operTime; }
    public String getRoomType() { return roomType; }
    public boolean isActive() { return active; }
}
