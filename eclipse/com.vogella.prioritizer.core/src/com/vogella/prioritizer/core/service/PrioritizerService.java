package com.vogella.prioritizer.core.service;

import java.util.List;

import com.vogella.prioritizer.core.model.Bug;

import io.reactivex.Single;

public interface PrioritizerService {
	public Single<byte[]> getKeyWordImage(String assignee, String product, String component, int limit);

	public Single<List<Bug>> getSuitableBugs(String assignee, String product, String component, int limit);
}
