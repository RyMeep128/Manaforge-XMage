# Manaforge XMage downstream

Upstream: https://github.com/magefree/mage.git (`upstream`, never pushed).
Integration branch: `manaforge/integration`; card work uses `manaforge/<feature>`.
Public fork: https://github.com/RyMeep128/Manaforge-XMage. The integration branch
is protected: pull requests and the engine check are required; force pushes and
deletion are disabled. Every PR runs the broad engine suite in CI. Pushes run the
focused card suite; a manual dispatch with broad=true runs the full suite.
No GitHub release or tag is created by the build workflow.

## Reproduce

Use Maven 3.9.9 and JDK 23, compiling for Java 8. Set MAVEN_OPTS=-Xmx6g.
Build: `mvn -B -ntp -pl Mage.Server,Mage.Client,Mage.Tests -am install -DskipTests`.
Focused test: `mvn -B -ntp -pl Mage.Tests -am test -Dtest=FeatherRadiantArbiterTest,ImmortalObligation*Test,HitItTest,MjolnirHammerOfThorTest -Dsurefire.failIfNoSpecifiedTests=false`.
Assembly: `mvn -B -ntp -pl Mage.Server,Mage.Client package assembly:single -DskipTests`.
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

The Hit It! backport uses the public-reveal Oracle correction documented at
https://magic.wizards.com/en/news/feature/star-trek-release-notes.
The original upstream implementation looked privately; the downstream regression
tests verify public reveal and that the graveyard movement is not milling.
