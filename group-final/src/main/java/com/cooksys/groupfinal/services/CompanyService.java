package com.cooksys.groupfinal.services;

import com.cooksys.groupfinal.dtos.*;

import java.util.Set;
import java.util.List;
import com.cooksys.groupfinal.dtos.AnnouncementDto;
import com.cooksys.groupfinal.dtos.FullUserDto;
import com.cooksys.groupfinal.dtos.ProjectDto;
import com.cooksys.groupfinal.dtos.TeamDto;
import com.cooksys.groupfinal.dtos.BasicUserDto;
import com.cooksys.groupfinal.dtos.CompanyDto;

public interface CompanyService {

	Set<BasicUserDto> getAllActiveUsers(Long id);
	
	Set<FullUserDto> getAllUsers(Long id);

    Set<AnnouncementDto> getAllAnnouncements(Long id);

    Set<TeamDto> getAllTeams(Long id);

    Set<ProjectDto> getAllProjects(Long companyId, Long teamId);

    BasicUserDto createUser(Long id, UserRequestDto userRequestDto);

    TeamDto createTeam(Long id, TeamDto teamDto);
    
    List<CompanyDto> getAllCompanies();

}
