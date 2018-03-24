package net.gmsworld.server.utils.persistence;

import java.util.List;

import javax.persistence.TypedQuery;

import net.gmsworld.server.persistence.Comment;

public class CommentPersistenceUtils {
     
	public void save(Comment c) {
		EMF.save(c);
	} 
	 
	public List<Comment> findByLandmark(int landmarkId) {
		TypedQuery<Comment> query = EMF.getEntityManager().createNamedQuery(Comment.FIND_BY_LANDMARK, Comment.class);
        query.setParameter("id", landmarkId);
		return query.getResultList(); 
    }
}
