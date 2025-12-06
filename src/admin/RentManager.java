package admin;

import java.util.ArrayList;
import java.util.List;

// 대여 내역을 관리하는 관리자용 매니저 (임시)
public class RentManager {

    private List<RentData> rentList = new ArrayList<>();

    public RentManager() {}

    public List<RentData> getRentList() {
        return rentList;
    }
}
