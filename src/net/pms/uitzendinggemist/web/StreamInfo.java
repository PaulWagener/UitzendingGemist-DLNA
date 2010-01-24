package net.pms.uitzendinggemist.web;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StreamInfo {
	private String XMLcontent;

	public StreamInfo(String XMLcontent) {
		this.XMLcontent = XMLcontent;
	}

	private String find(String regex) {
		Matcher m = Pattern.compile(regex, Pattern.DOTALL).matcher(XMLcontent);
		if (!m.find())
			return null;
		return m.group(1).trim();
	}


	public String getMediaStream() {
		return find("\"(mms://.*?)\"");
	}

}
