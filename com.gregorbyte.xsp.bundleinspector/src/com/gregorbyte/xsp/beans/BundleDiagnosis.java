package com.gregorbyte.xsp.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BundleDiagnosis implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	private String location;
	private String id;

	private List<String> resolverErrors;

	private String unresolved;

	private StringBuilder sb = new StringBuilder();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUnresolved() {
		return unresolved;
	}

	public void setUnresolved(String unresolved) {
		this.unresolved = unresolved;
	}

	public String getMessage() {
		return sb.toString();
	}

	public void addResolverError(String error) {
		if (resolverErrors == null) {
			resolverErrors = new ArrayList<String>();
		}
		resolverErrors.add(error);
	}

	public void print(String msg) {
		sb.append(msg);
	}

	public void println(String msg) {
		sb.append(msg);
		sb.append("\n");
	}

}
