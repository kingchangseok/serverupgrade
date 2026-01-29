package app.ecams.history.dao;

import java.util.List;

import app.ecams.history.model.HistoryInfo;

public interface IHistoryDAO {
	List<HistoryInfo> getHistoryList_detail(String itemid);
}
