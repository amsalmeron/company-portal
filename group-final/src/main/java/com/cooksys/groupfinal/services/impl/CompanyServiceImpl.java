package com.cooksys.groupfinal.services.impl;

import static com.cooksys.twitter_api.helpers.Helpers.parseAndSaveHashtags;
import static com.cooksys.twitter_api.helpers.Helpers.parseAndSaveMentions;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.cooksys.groupfinal.dtos.AnnouncementDto;
import com.cooksys.groupfinal.dtos.CredentialsDto;
import com.cooksys.groupfinal.dtos.FullUserDto;
import com.cooksys.groupfinal.dtos.ProjectDto;
import com.cooksys.groupfinal.dtos.TeamDto;
import com.cooksys.groupfinal.entities.Announcement;
import com.cooksys.groupfinal.entities.Company;
import com.cooksys.groupfinal.entities.Credentials;
import com.cooksys.groupfinal.entities.Project;
import com.cooksys.groupfinal.entities.Team;
import com.cooksys.groupfinal.entities.User;
import com.cooksys.groupfinal.exceptions.BadRequestException;
import com.cooksys.groupfinal.exceptions.NotFoundException;
import com.cooksys.groupfinal.mappers.AnnouncementMapper;
import com.cooksys.groupfinal.mappers.CredentialsMapper;
import com.cooksys.groupfinal.mappers.ProjectMapper;
import com.cooksys.groupfinal.mappers.TeamMapper;
import com.cooksys.groupfinal.mappers.FullUserMapper;
import com.cooksys.groupfinal.repositories.AnnouncementRepository;
import com.cooksys.groupfinal.repositories.CompanyRepository;
import com.cooksys.groupfinal.repositories.ProjectRepository;
import com.cooksys.groupfinal.repositories.TeamRepository;
import com.cooksys.groupfinal.services.CompanyService;
import com.cooksys.twitter_api.entities.Tweet;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {
	
	private final CompanyRepository companyRepository;
	private final AnnouncementRepository announcementRepository;
	private final ProjectRepository projectRepository;
	private final TeamRepository teamRepository;
	private final FullUserMapper fullUserMapper;
	private final AnnouncementMapper announcementMapper;
	private final TeamMapper teamMapper;
	private final ProjectMapper projectMapper;
	
	private final CredentialsMapper credentialsMapper;

	
	private Company findCompany(Long id) {
        Optional<Company> company = companyRepository.findById(id);
        if (company.isEmpty()) {
            throw new NotFoundException("A company with the provided id does not exist.");
        }
        return company.get();
    }
	
	private Team findTeam(Long id) {
        Optional<Team> team = teamRepository.findById(id);
        if (team.isEmpty()) {
            throw new NotFoundException("A team with the provided id does not exist.");
        }
        return team.get();
    }
	
	@Override
	public Set<FullUserDto> getAllUsers(Long id) {
		Company company = findCompany(id);
		Set<User> filteredUsers = new HashSet<>();
		company.getEmployees().forEach(filteredUsers::add);
		filteredUsers.removeIf(user -> !user.isActive());
		return fullUserMapper.entitiesToFullUserDtos(filteredUsers);
	}

	@Override
	public Set<AnnouncementDto> getAllAnnouncements(Long id) {
		Company company = findCompany(id);
		List<Announcement> sortedList = new ArrayList<Announcement>(company.getAnnouncements());
		sortedList.sort(Comparator.comparing(Announcement::getDate).reversed());
		Set<Announcement> sortedSet = new HashSet<Announcement>(sortedList);
		return announcementMapper.entitiesToDtos(sortedSet);
	}

	@Override
	public Set<TeamDto> getAllTeams(Long id) {
		Company company = findCompany(id);
		return teamMapper.entitiesToDtos(company.getTeams());
	}

	@Override
	public Set<ProjectDto> getAllProjects(Long companyId, Long teamId) {
		Company company = findCompany(companyId);
		Team team = findTeam(teamId);
		if (!company.getTeams().contains(team)) {
			throw new NotFoundException("A team with id " + teamId + " does not exist at company with id " + companyId + ".");
		}
		Set<Project> filteredProjects = new HashSet<>();
		team.getProjects().forEach(filteredProjects::add);
		filteredProjects.removeIf(project -> !project.isActive());
		return projectMapper.entitiesToDtos(filteredProjects);
	}

	@Override
	public AnnouncementDto createAnnouncement(Long id, AnnouncementDto announcementDto, CredentialsDto credentialsDto) {

 		
		if(credentialsDto == null) {
			
			throw new BadRequestException("Bad Credentials Dto");
		}

		//Credentials credentials = credentialsMapper.dtoToEntity(credentialsDto);
		 		
		if(!announcementDto.getAuthor().isAdmin()) {
			
			throw new BadRequestException("User is not Admin, cannot post announcement.");
		}
		
		Optional<Company> selectedCompany = companyRepository.findByIdAndDeletedFalse(id);
		
		if(selectedCompany.isEmpty()) {
			
			throw new NotFoundException("Company not found with given id");
		}
		
		if(announcementDto.getMessage().isBlank()) {
			
			throw new BadRequestException("Announcement Message cannot be empty");
		}
		
 		
		Announcement announcementToPost = announcementMapper.dtoToEntity(announcementDto);
		announcementToPost.setAuthor(announcementDto.getAuthor());		// need to fix this line
		announcementToPost.setTitle(announcementDto.getTitle());
		selectedCompany.get().getAnnouncements().add(announcementToPost);
		announcementToPost.setDate(new Timestamp(System.currentTimeMillis()));
		
			 
 		return announcementMapper.entityToDto(announcementRepository.saveAndFlush(announcementToPost));  
				
 	}

	@Override
	public ProjectDto updateProject(Long id, Long teamID, CredentialsDto credentialsDto, ProjectDto projectDto) {

		
		if(credentialsDto == null) {
			
			throw new BadRequestException("Bad Credentials Dto");
		}

		Optional<Company> selectedCompany = companyRepository.findByIdAndDeletedFalse(id);
		
		if(selectedCompany.isEmpty()) {
			
			throw new NotFoundException("Company not found with given id");
		}
		
		Optional<Team> selectedTeam = teamRepository.findById(teamID);

		if(selectedTeam.isEmpty()) {
			
			throw new NotFoundException("Company not found with given id");
		}
		
		if(!selectedTeam.get().getCompany().equals(selectedCompany)) {
			
			throw new BadRequestException("Specified team doesn't belong to the selected company");
		
		}
		
		if(projectDto.getName().isBlank()) {
			
			throw new BadRequestException("Project Name cannot be empty");
		}
		
	
		Project projectToUpdate = projectMapper.dtoToEntity(projectDto);
		
		if(projectToUpdate.isActive()) {
			
			projectToUpdate.setName(projectDto.getName());
			projectToUpdate.setDescription(projectDto.getDescription());
			projectToUpdate.setTeam(selectedTeam.get());
						
		}
		
		
		return projectMapper.entityToDto(projectRepository.saveAndFlush(projectToUpdate));
		
		
	}


}
