package net.sf.eclipsefp.haskell.scion.internal.commands;

import java.util.ArrayList;
import java.util.List;

import net.sf.eclipsefp.haskell.scion.types.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Command that lists the places where a given identifier is defined.
 * 
 * @author Thomas ten Cate
 */
public class NameDefinitionsCommand extends ScionCommand {

	private String name;
	
	private List<Location> locations;

	public NameDefinitionsCommand(String name) {
		super();
		this.name = name;
	}
	
	@Override
  public String getMethod() {
		return "name-definitions";
	}

	@Override
	protected JSONObject getParams() throws JSONException {
		JSONObject params = new JSONObject();
		params.put("name", name);
		return params;
	}

	@Override
	protected void doProcessResult() throws JSONException {
		JSONArray result = (JSONArray) response;
		locations = new ArrayList<Location>(result.length());
		for (int i = 0; i < result.length(); ++i) {
			JSONObject obj=result.getJSONObject(i);
			if (obj.opt("no-location")==null){
				locations.add(new Location(obj));
			} 
		}
	}

	public boolean isFound() {
		return locations != null && locations.size() > 0;
	}
	
	public List<Location> getLocations(int index) {
		return locations;
	}
	
	public Location getFirstLocation() {
		if (locations.isEmpty()){
			return null;
		}
		return locations.iterator().next();
	}

}
