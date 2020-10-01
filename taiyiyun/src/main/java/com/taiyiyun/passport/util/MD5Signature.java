package com.taiyiyun.passport.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MD5Signature {
	public static String signMd5(Map<String, String> dataMap, String secret) {
		if (null == dataMap || dataMap.size() == 0) {
			return null;
		}

		Map<String, String> params = new HashMap<String, String>();
		for (Iterator<String> it = dataMap.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			if (StringUtil.isNotEmpty(dataMap.get(key))) {
				params.put(key, dataMap.get(key));
			}
		}

		String serializeParam = serialize(sortMap(params), secret);
		String sign = "";
		try {
			sign = MD5Util.MD5Encode(serializeParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sign;
	}

	private static List<Entry<String, String>> sortMap(Map<String, String> dataMap) {
		if (null == dataMap || dataMap.size() == 0) {
			return null;
		}
		
		List<Entry<String, String>> entries = new ArrayList<Entry<String, String>>(dataMap.entrySet());
		Collections.sort(entries, new Comparator<Entry<String, String>>() {
			@Override
			public int compare(Entry<String, String> o1, Entry<String, String> o2) {

				return o1.getKey().toString().compareTo(o2.getKey().toString());
			}
		});

		return entries;
	}

	private static String serialize(List<Entry<String, String>> dataList, String secret) {
		if (dataList == null || dataList.size() == 0) {
			return null;
		}

		StringBuffer bf = new StringBuffer(secret);
		for (int i = 0; i < dataList.size(); i++) {
			Entry<String, String> entry = dataList.get(i);
			bf.append(entry.getKey()).append(entry.getValue());
		}
		bf.append(secret);
		
		return bf.toString();
	}

}
