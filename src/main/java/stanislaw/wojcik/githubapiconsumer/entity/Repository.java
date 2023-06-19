package stanislaw.wojcik.githubapiconsumer.entity;

import java.util.List;

public record Repository(String repositoryName, String ownerLogin, List<Branch> branchList) {}
