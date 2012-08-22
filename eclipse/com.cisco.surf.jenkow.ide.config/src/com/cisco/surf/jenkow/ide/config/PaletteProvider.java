package com.cisco.surf.jenkow.ide.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.activiti.designer.integration.servicetask.CustomServiceTask;
import org.activiti.designer.integration.servicetask.CustomServiceTaskDescriptor;
import org.activiti.designer.eclipse.extension.palette.IPaletteProvider;

public class PaletteProvider implements IPaletteProvider {
	private String jenkinsTaskExtensionJarPath;

	@Override
	public List<CustomServiceTaskDescriptor> provide(){
		List<CustomServiceTaskDescriptor> providers = new ArrayList<CustomServiceTaskDescriptor>();
		providers.add(new CustomServiceTaskDescriptor(JenkinsTask.class,"Jenkins",getExtensionJarPath()));
		return providers;
	}
	
	private String getExtensionJarPath(){
		if (jenkinsTaskExtensionJarPath == null){
			try {
				Properties props = new Properties();
				props.load(getClass().getResourceAsStream("/build.env.properties"));
				String designerExtensionName = props.getProperty("designer.extension.name");
				
				String home = System.getProperty("user.home");
				File dst = new File(home+"/.jenkow/lib/"+designerExtensionName);
				jenkinsTaskExtensionJarPath = dst.getAbsolutePath();
				
				if (!dst.exists()){
					// TODO 8: close streams in exception case
					InputStream in = getClass().getResourceAsStream("/"+designerExtensionName);
					dst.getParentFile().mkdirs();
					byte[] buf = new byte[1024];
					FileOutputStream out = new FileOutputStream(dst);
					while (true){
						int n = in.read(buf);
						if (n < 0) break;
						out.write(buf,0,n);
					}
					out.close();
					in.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return jenkinsTaskExtensionJarPath;
	}
}
