package com.wonkglorg.doc.api.service;

import com.wonkglorg.doc.core.FileRepository;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.response.QueryDatabaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class RepoAsyncOperations{
	private static final Logger log = LoggerFactory.getLogger(RepoAsyncOperations.class);
	
	@Async
	public CompletableFuture<Void> initializeRepositoryAsync(FileRepository repository) {
		return CompletableFuture.runAsync(() -> {
			try{
				log.info("Initializing Repo '{}'", repository.getRepoProperties().getId());
				repository.initialize();
			} catch(Exception e){
				log.error("Failed to initialize repository '{}'", repository.getRepoProperties().getId(), e);
			}
		});
	}



	/*
	
	// Perform search within a single repository asynchronously
	@Async
	public CompletableFuture<List<Resource>> searchInRepositoryAsync(FileRepository repository, String searchTerm) {
		return CompletableFuture.supplyAsync(() -> {
			try{
				log.info("Searching in Repo '{}' for term '{}'", repository.getRepoProperties().getName(), searchTerm);
				return repository.search(searchTerm); // Assuming 'search' is a method in FileRepository
			} catch(Exception e){
				log.error("Error searching in repository '{}'", repository.getRepoProperties().getName(), e);
				return new ArrayList<Resource>(); // Return empty list in case of error
			}
		});
	}
	
	 */
	
	@Async
	public CompletableFuture<QueryDatabaseResponse<List<Resource>>> getResourcesFromRepositoryAsync(FileRepository repository) {
		//return CompletableFuture.supplyAsync(() -> repository.getDatabase().getResources());
		return null;
	}
	
}
