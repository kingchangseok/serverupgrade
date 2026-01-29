package app.ecams.request.lastcheckout.dao;

import java.util.HashMap;
import java.util.List;

import app.ecams.request.lastcheckout.model.LastCheckOutInfo;

public interface ILastCheckOutDAO {
	public List<LastCheckOutInfo> select_lastver(String itemid);
}
