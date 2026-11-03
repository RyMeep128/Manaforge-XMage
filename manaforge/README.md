# Manaforge XMage downstream

Upstream: https://github.com/magefree/mage.git (`upstream`, never pushed).
Integration branch: `manaforge/integration`; card work uses `manaforge/<feature>`.
This local fork has not been published. Configure origin and remote branch protection
when publishing: require pull requests, passing focused/compatibility checks, and
prevent force pushes/deletion. Local branches cannot provide server-side protection.

## Reproduce

Use Maven 3.9.9 and JDK 23, compiling for Java 8. Set MAVEN_OPTS=-Xmx6g.
Build: `mvn -B -ntp -pl Mage.Server,Mage.Client,Mage.Tests -am install -DskipTests`.
Focused test: `mvn -B -ntp -pl Mage.Tests -am test -Dtest=FeatherRadiantArbiterTest,ImmortalObligationTest -Dsurefire.failIfNoSpecifiedTests=false`.
Assembly: `mvn -B -ntp -pl Mage.Server,Mage.Client assembly:single`.
Set `-Dproject.build.outputTimestamp=<source commit ISO timestamp>` for builds and
assemblies. All modules must come from this checkout, never overlaid on another build.

## Upstream update

1. Fetch upstream without changing integration. Record the exact new revision.
2. Read changes.json; identify adopted/replaced local changes by behavior and tests.
3. Create a new worktree/branch from the candidate revision. Replay retained commits
   in dependency order; resolve conflicts explicitly. Never force-update integration.
4. Build all production modules and run local regression and compatibility suites.
   Preserve tests when replacing local code with an upstream implementation.
5. Initialize a fresh catalog and stage complete server/client assemblies.
6. Pin the candidate lock in Manaforge, run packaged acceptance, and compare coverage.
7. Promote only the tested source and artifact lock. Keep previous bundles for rollback.
   New sessions select the new bundle; never switch a running session's engine.

The London mulligan patch in Manaforge is optional and is NOT included here.
Feather and Immortal Obligation use independent XMage implementations. Forge card
scripts are behavior references, not build/runtime dependencies. Keep upstream MIT
license and all dependency notices in distributions. Never add raw card artwork.
