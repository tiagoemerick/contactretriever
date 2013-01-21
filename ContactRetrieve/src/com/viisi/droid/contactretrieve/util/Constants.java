package com.viisi.droid.contactretrieve.util;

public final class Constants {

	private Constants() {
	}

	public static final class layout {
		public static final String string_layout_preferences = "layoutdestination";
	}
	
	public static final class preferences {
		public static final String preferencefilename = "preferencescr";
		public static final String preference_master_password = "masterpassword";
		public static final String preference_mail = "mailrecover";
	}

	public static final class apprater {
		public static final String preferencefilename = "apprater";
		public static final String preference_dontshowagain = "dontshowagain";
		public static final String preference_launch_count = "launch_count";
		public static final String preference_date_firstlaunch = "date_firstlaunch";
	}

	public static final class mail {
		public static final String mail_send_status = "mailsendstatus";
		public static final String mail_send_fakefrom = "noreply@contact.com";
		public static final String mail_send_from = "contactretrieve@gmail.com";
		public static final String mail_send_password = "javazouk";
	}

	public static final class sendsms {
		public static final String celnumber = "celNumber";
		public static final String textmessage = "textMessage";
		public static final String idpasswordused = "idPasswordUsed";
		public static final String requesttype = "requestType";
		public static final String requesttype_contact = "contactRequest";
		public static final String requesttype_password = "passwordRequest";
	}

	public static final class wildcards {
		public static final String string_password_request = "$";
		public static final String string_password_separator = "!";
		public static final String string_contact_request = "#1";
	}

}
