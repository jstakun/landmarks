package net.gmsworld.server.utils.persistence;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import net.gmsworld.server.persistence.Comment;

@Stateless
public class CommentPersistenceUtils {
     
	@PersistenceContext
    private EntityManager entityManager;
	
	public void save(Comment c) {
		entityManager.persist(c);
		entityManager.flush();
	} 
	 
	public List<Comment> findByLandmark(int landmarkId) {
		TypedQuery<Comment> query = entityManager.createNamedQuery(Comment.FIND_BY_LANDMARK, Comment.class);
        query.setParameter("id", landmarkId);
		return query.getResultList(); 
    }
}
