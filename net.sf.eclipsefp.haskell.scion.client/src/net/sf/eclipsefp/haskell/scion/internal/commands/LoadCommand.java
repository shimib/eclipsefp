package net.sf.eclipsefp.haskell.scion.internal.commands;

import net.sf.eclipsefp.haskell.scion.client.IScionServer;
import net.sf.eclipsefp.haskell.scion.client.ScionPlugin;
import net.sf.eclipsefp.haskell.scion.internal.servers.IScionCommandRunner;
import net.sf.eclipsefp.haskell.scion.internal.util.UITexts;
import net.sf.eclipsefp.haskell.scion.types.CompilationResult;
import net.sf.eclipsefp.haskell.scion.types.Component;
import net.sf.eclipsefp.haskell.scion.types.ICompilerResult;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Command that loads a given component into the Scion server. 
 * 
 * @author Thomas ten Cate
 */
public class LoadCommand extends ScionCommand implements ICompilerResult{

	
	private Component comp;
	private CompilationResult compilationResult;
	private boolean output;
	private boolean forceRecomp;
	
	public LoadCommand(IScionCommandRunner runner, IScionServer server, Component c, boolean output, 
	    boolean forceRecomp) {
		super(runner, server, Job.BUILD);
		this.comp=c;
		this.output=output;
		this.forceRecomp=forceRecomp;
	}
	
	@Override
	protected String getMethod() {
		return "load";
	}

	@Override
	protected JSONObject getParams() throws JSONException {
		JSONObject params = new JSONObject();
		params.put("component", comp.toJSON());
		JSONObject options=new JSONObject();
		options.put("output",output);
		options.put("forcerecomp",forceRecomp);
		params.put("options", options);
		return params;
	}

	@Override
	protected void doProcessResult(Object result) throws JSONException {
		compilationResult = new CompilationResult((JSONObject)result);

	}
	
	public CompilationResult getCompilationResult() {
		return compilationResult;
	}
	
	public boolean hasOutput() {
		return output;
	}
	
	@Override
	protected boolean onError(JSONException ex, String name, String message) {
		try {
			IMarker marker = getRunner().getProject().createMarker(IMarker.PROBLEM);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			marker.setAttribute(IMarker.MESSAGE,message);
		} catch (CoreException ce){
			ScionPlugin.logError(UITexts.error_applyMarkers, ce);
			ce.printStackTrace();
		}
		return true;
	}

}
