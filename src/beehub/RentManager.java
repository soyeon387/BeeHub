package beehub;

import java.util.ArrayList;
import java.util.List;

public class RentManager {

    private List<RentData> rentList = new ArrayList<>();

    public RentManager() {}

    // MainFrame에서 getAllRentals() 를 호출하니까 이 메서드 추가
    public List<RentData> getAllRentals() {
        return rentList;
    }

    // 대여 내역 추가용 (나중에 사용할 수 있게)
    public void addRental(RentData data) {
        rentList.add(data);
    }
}
