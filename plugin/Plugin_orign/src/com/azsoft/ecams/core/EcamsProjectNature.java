package com.azsoft.ecams.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * 
 * 형상관리 프로젝트 생성 또는, 형상관리사용시 eCAMS전용 Nature 생성 및 제거
 * 클래스상수 NATURE_ID의 값은 plugin.xml의 nature확장점의 id와 동일
 * setEcamsNature()호출시 nature 설정 (내부적으로 setProject, configure이 순서대로 호출됨)
 * delEcamsNature()호출시 nature 제거 (내부적으로 deconfigure이 호출됨)
 * 
 */

public class EcamsProjectNature implements IProjectNature  {
	
	public static final String NATURE_ID = "com.azsoft.ecams.nature";
	private IProject project;
		
	
	@Override
	public void configure() throws CoreException {
	}

	@Override
	public void deconfigure() throws CoreException {
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}
	
	public static void setEcamsNature(IProject project) {
		try {
			IProject p = project;
			IProjectDescription description = p.getDescription();
			List<String> natureIds = new ArrayList<String>();
			natureIds.addAll(Arrays.asList(description.getNatureIds()));
			natureIds.add(NATURE_ID);
			description.setNatureIds(natureIds.toArray(new String[natureIds.size()]));
			p.setDescription(description, new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static void delEcamsNature(IProject project) {
		try {
			IProject p = project;
			IProjectDescription description = p.getDescription();
			List<String> natureIds = new ArrayList<String>();
			List<String> tmpNatureIds = new ArrayList<String>();
						 tmpNatureIds = Arrays.asList(description.getNatureIds());
			int natureCnt = description.getNatureIds().length;
	
			for(int i=0; i < natureCnt; i++) {
				if( !tmpNatureIds.get(i).equals(NATURE_ID) ){
					natureIds.add(tmpNatureIds.get(i));
				}
			}
			
			description.setNatureIds(natureIds.toArray(new String[natureIds.size()]));
			p.setDescription(description, new NullProgressMonitor());
			
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

}
