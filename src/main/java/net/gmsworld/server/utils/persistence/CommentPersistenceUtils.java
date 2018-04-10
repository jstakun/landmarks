package net.gmsworld.server.utils.persistence;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import net.gmsworld.server.persistence.Comment;

public class CommentPersistenceUtils {
     
	public void save(Comment c, EntityManager entityManager) {
		EMF.save(c, entityManager);
	} 
	 
	public List<Comment> findByLandmark(int landmarkId, EntityManager entityManager) {
		TypedQuery<Comment> query = entityManager.createNamedQuery(Comment.FIND_BY_LANDMARK, Comment.class);
        query.setParameter("id", landmarkId);
		return query.getResultList(); 
    }
}
