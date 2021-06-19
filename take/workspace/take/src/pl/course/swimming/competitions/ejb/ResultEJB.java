package pl.course.swimming.competitions.ejb;

import java.util.LinkedList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import pl.course.swimming.competitions.dto.ResultDto;
import pl.course.swimming.competitions.exceptions.IdNotFoundException;
import pl.course.swimming.competitions.model.Competition;
import pl.course.swimming.competitions.model.Discipline;
import pl.course.swimming.competitions.model.Result;
import pl.course.swimming.competitions.model.Swimmer;

/**
 * Result service
 * @version 1.0
 * @category EJB
 * @see pl.course.swimming.competitions.rest.ResultREST
 * */
@Stateless
public class ResultEJB {

	@PersistenceContext(name="result")
	EntityManager manager;
	
	/**
	 * Add new result
	 * @param resultDto DTO object of result model
	 * */
	public void create(ResultDto resultDto) {
		
		Result result = new Result();
		
		Swimmer swimmer = manager.find(Swimmer.class, resultDto.getSwimmerIdc());
		Discipline discipline = manager.find(Discipline.class, resultDto.getDisciplineIdc());
		Competition competition = manager.find(Competition.class, resultDto.getCompetitionIdc());
		
		try {
			this.validateRelation(resultDto, swimmer, discipline, competition);
		} catch(IdNotFoundException ex) {
			throw ex;
		}
		
		result.update(resultDto, swimmer, discipline, competition);
		
		manager.persist(result);
	}
	
	/**
	 * Update result
	 * @param resultDto DTO object of result model
	 * @param idc id of result
	 * */
	public void update(ResultDto resultDto, long idc) {
		
		Result result;
		
		try {
			result = manager.getReference(Result.class, idc);
		} catch(RuntimeException ignore) {
			throw new IdNotFoundException(idc, "RESULT");
		}
		
		Swimmer swimmer = manager.find(Swimmer.class, resultDto.getSwimmerIdc());
		Discipline discipline = manager.find(Discipline.class, resultDto.getDisciplineIdc());
		Competition competition = manager.find(Competition.class, resultDto.getCompetitionIdc());
		
		try {
			this.validateRelation(resultDto, swimmer, discipline, competition);
		} catch(IdNotFoundException ex) {
			throw ex;
		}
		
		result.update(resultDto, swimmer, discipline, competition);
		
		manager.merge(result);
	}
	
	/**
	 * Filter result by swimmer, discipline and competition id
	 * @param swimmerIdc id of swimmer
	 * @param disciplineIdc id of discipline
	 * @param competitionIdc id of competition
	 * @return list of results which have same values as passed argument
	 * */
	public List<Result> fetchByAll(Long swimmerIdc, Long disciplineIdc, Long competitionIdc)
	{
		return this.fetch(swimmerIdc, disciplineIdc, competitionIdc);
	}
	
	/**
	 * Filter result by swimmer id
	 * @param idc id of swimmer
	 * @return list of results which have same swimmer.idc as passed argument
	 * */
	public List<Result> fetchBySwimmerIdc(Long idc) {
		return this.fetch(idc, null, null);
	}

	/**
	 * Filter result by discipline id
	 * @param idc id of discipline
	 * @return list of results which have same discipline.idc as passed argument
	 * */
	public List<Result> fetchByDisciplineIdc(Long idc) {
		return this.fetch(null, idc, null);
	}

	/**
	 * Filter result by competition id
	 * @param idc id of competition
	 * @return list of results which have same competition.idc as passed argument
	 * */
	public List<Result> fetchCompetitonIdc(Long idc) {
		return this.fetch(null, null, idc);
	}
	
	private List<Result> fetch(Long swimmerIdc, Long disciplineIdc, Long competitionIdc) {
		
		StringBuffer query = new StringBuffer("select r.idc from Result r");
		
		if (swimmerIdc != null) {
			query.append(" INNER JOIN r.swimmer s on s.idc = " + swimmerIdc);
		}
		
		if (disciplineIdc != null) {
			query.append(" INNER JOIN r.discipline d on d.idc = " + disciplineIdc);
		}
		
		if (competitionIdc != null) {
			query.append( " INNER JOIN r.competition c on c.idc = " + competitionIdc);
		}
		
		Query q = manager.createQuery(query.toString(), Long.class);
		
		@SuppressWarnings("unchecked")
		List<Long> idcs = q.getResultList();
		
		List<Result> list = new LinkedList<Result>();
		
		for (Long idc : idcs) {
			Result result = manager.getReference(Result.class, idc);
			list.add(new Result(result));
		}
		
		return list;
	}
	
	/**
	 * Get result by id
	 * @param idc id of result
	 * @return result which has same id as passed argument
	 * */
	public Result find(long idc) {
		Result r = manager.find(Result.class, idc);
		return r;
	}

	/**
	 * Remove result with proper id
	 * @param idc id of result
	 * */
	public void delete(long idc) {
		Result result = manager.find(Result.class, idc);
		
		if (result == null) 
			throw new IdNotFoundException(idc, "RESULT");
		
		manager.remove(result);
	}
	
	/**
	 * Validate relations
	 * @param resultDto
	 * @param swimmer
	 * @param discipline
	 * @param competition
	 * **/
	private void validateRelation(ResultDto resultDto, Swimmer swimmer, Discipline discipline, Competition competition) throws IdNotFoundException{
		if(swimmer == null ) {
			Long id = new Long(resultDto.getSwimmerIdc());
			throw new IdNotFoundException(id, "SWIMMER");
		}
		
		if(discipline == null ) {
			Long id = new Long(resultDto.getDisciplineIdc());
			throw new IdNotFoundException(id, "DISCIPLINE");
		}
		
		if(competition == null ) {
			Long id = new Long(resultDto.getCompetitionIdc());
			throw new IdNotFoundException(id, "COMPETITION");
		}
	}
}
