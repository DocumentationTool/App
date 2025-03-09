package com.wonkglorg.doc.core.git;

import com.wonkglorg.doc.core.objects.Resource;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Represents a branch for a user in a git repo
 */
public class UserBranch{
	private final GitRepo repo;
	//todo:jmd figure out a nice branch naming
	private final String branchName;
	private final String userId;
	private Ref branch;
	
	public UserBranch(GitRepo repo, String userId) throws GitAPIException {
		this.repo = repo;
		this.userId = userId;
		this.branchName = "user/" + userId + "/" + UUID.randomUUID();
		try{
			this.branch = repo.getGit().getRepository().findRef(branchName);
		} catch(IOException e){
			throw new RuntimeException(e);
		}
		
		if(this.branch == null){
			createBranch();
		}
	}
	
	/**
	 * Adds a file to the user branch (stages the file) this should be called everytime this file changes, before committing
	 *
	 * @param file The file to add
	 * @throws GitAPIException If an error occurs while adding the file
	 */
	public void addFile(Path file) {
		Git git = repo.getGit();
		
		String repoRelativePath = git.getRepository().getWorkTree().toPath().relativize(file).toString();
		
		try{
			git.checkout().setName(branchName).call();
			git.add().addFilepattern(repoRelativePath).call();
		} catch(GitAPIException e){
			
			throw new RuntimeException(e);
		}
	}
	
	public void addResource(Resource resource) {
		Git git = repo.getGit();
		String repoRelativePath = git.getRepository().getWorkTree().toPath().relativize(resource.resourcePath()).toString();
		
		if(!Files.exists(resource.resourcePath())){
			try{
				Files.createFile(resource.resourcePath());
				Files.write(resource.resourcePath(), resource.data().getBytes());
				git.add().addFilepattern(repoRelativePath).call();
			} catch(IOException | GitAPIException e){
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * Removes a file from the user branch (unstages the file) and deletes it from the file system
	 *
	 * @param file
	 */
	public void removeFile(Path file) {
		Git git = repo.getGit();
		String repoRelativePath = git.getRepository().getWorkTree().toPath().relativize(file).toString();
		
		try{
			git.checkout().setName(userId).call();
			git.rm().addFilepattern(repoRelativePath).call();
		} catch(GitAPIException e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Updates a file that has been deleted from the file system (does not do anything to the file system only deletes the file from the git cache) should be used when a file was deleted by hand and not through the application
	 *
	 * @param file The file that was deleted
	 */
	public void updateFileDeleted(Path file) {
		Git git = repo.getGit();
		try{
			git.checkout().setName(branchName).call();
			git.rm().setCached(true).addFilepattern(file.toString()).call();
		} catch(GitAPIException e){
			throw new RuntimeException(e);
		}
	}
	
	public void commit(String message) {
		Git git = repo.getGit();
		try{
			git.checkout().setName(branchName).call();
			git.commit().setMessage(message).setAuthor(userId, "email@example.com").call();
		} catch(GitAPIException e){
			throw new RuntimeException(e);
		}
		
	}
	
	public void push(String username, String password) throws GitAPIException {
		Git git = repo.getGit();
		git.checkout().setName(branchName).call();
		
		git.push().setRemote("origin").setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password)).call();
	}
	
	public void closeBranch() throws GitAPIException {
		Git git = repo.getGit();
		git.checkout().setName(repo.getMasterBranchName()).call();  // Switch to another branch before deleting
		git.branchDelete().setBranchNames(branchName).setForce(true).call();
	}
	
	public void createBranch() throws GitAPIException {
		Git git = repo.getGit();
		this.branch = git.branchCreate().setName(branchName).call();
	}
	
	/**
	 * Merges the user branch into the main branch
	 *
	 * @throws IOException
	 * @throws GitAPIException
	 */
	public void mergeIntoMain() throws IOException, GitAPIException {
		Git git = repo.getGit();
		
		// Ensure the branch exists before merging
		if(git.getRepository().findRef(branchName) == null){
			throw new GitAPIException("Branch does not exist: " + branchName){};
		}
		
		// Switch to main branch before merging
		git.checkout().setName(repo.getMasterBranchName()).call();
		
		// Merge user branch into main
		MergeResult mergeResult = git.merge().include(git.getRepository().findRef(branchName)).call();
		
		// Check merge result
		if(!mergeResult.getMergeStatus().isSuccessful()){
			throw new GitAPIException("Merge conflict occurred: " + mergeResult.getMergeStatus()){};
		}
	}
}
