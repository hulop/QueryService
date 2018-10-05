package org.hulop.data.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

public class Messages {

	private static ResourceBundle.Control control = new ResourceBundle.Control() {
		public static final String XML = "xml";
		public List<String> getFormats(String baseName) {
			return Arrays.asList(XML);
		}
		public ResourceBundle newBundle(String baseName, Locale locale,
				String format, ClassLoader loader, boolean reload)
				throws IllegalAccessException, InstantiationException, IOException {
			if (baseName == null || locale == null || format == null
					|| loader == null) {
				throw new NullPointerException();
			}

			ResourceBundle bundle = null;
			if (format.equals(XML)) {
				String bundleName = toBundleName(baseName, locale);
				String resourceName = toResourceName(bundleName, format);
				InputStream stream = null;
				URL url = loader.getResource(resourceName);

				if (url != null) {
					URLConnection connection = url.openConnection();
					if (connection != null) {
						if (reload) {
							connection.setUseCaches(false);
						}
						stream = connection.getInputStream();
					}
				}

				if (stream != null) {
					Properties props = new Properties();
					props.loadFromXML(stream);

					return new ResourceBundle() {
						@Override
						protected Object handleGetObject(String key) {
							return props.getProperty(key);
						}
						@SuppressWarnings("unchecked")
						@Override
						public Enumeration<String> getKeys() {
							return (Enumeration<String>) props.propertyNames();
						}
					};
				}
			}
			return bundle;
		}

		@Override
		public Locale getFallbackLocale(String baseName, Locale locale) {
			return null;
		}
	};
	
	
	public static ResourceBundle getBundle(Locale locale) {
		return ResourceBundle.getBundle("org.hulop.data.i18n.Messages", locale, control);
	}
	
	public static String get(Locale locale, String key) {
		ResourceBundle resource = getBundle(locale);
		if (resource != null) {
			try {
				return resource.getString(key);
			} catch (Exception e) {
				return "___"+key+"___no_entry";
			}
		}
		return "___"+key+"___no_resource";
	}	
}
