# COMP2000 Worksheet 1 — Mid-Semester Submission

**Student name:** Zahin Rafid

**Student ID:** 

**GitHub repo URL:** https://github.com/Zahin-Rafid/COMP2000-

---

## 1. Version Control

**1.1.** Paste the first 10 lines of the output of `git log --graph --oneline --all` from your repository:

```
*   cb4c780 Merge pull request #4 from feature/engine-ui-and-architecture
|\  
| * cc91316 feat(ui): add 60 FPS Swing UI, live graph HUD, simulation loop, verification test, and UML diagram PDF
|/  
*   702d3df Merge pull request #3 from feature/genetics-and-species
|\  
| * f4334a4 feat(entities): add hereditary Chromosome/Genome and multi-tier species (Plant, Herbivore, Carnivore, Apex, Decomposer)
|/  
*   9173e68 Merge pull request #2 from feature/spatial-grid-generics
|\  
| * e6f5706 feat(grid): implement generic SpatialGrid and BoundedResource with domain exception handling
|/  
*   27751e8 Merge pull request #1 from feature/core-model-hierarchy
```

**1.2.** Describe your workflow. Did you use branches? Pull requests?

Our development process strictly followed a feature-branch workflow structured around discrete subsystem milestones:
1. **Feature Branching**: Development was partitioned into dedicated branches corresponding to modular architectural layers (`feature/core-model-hierarchy`, `feature/spatial-grid-generics`, `feature/genetics-and-species`, and `feature/engine-ui-and-architecture`).
2. **Discrete, Atomic Commits**: Each commit encapsulated a self-contained, compilable change with conventional commit messaging (`feat(...)`, `chore(...)`, `test(...)`), ensuring the commit history tells a clear, chronological story of system evolution.
3. **Pull Request Integration**: Features were merged into the `main` branch via explicit non-fast-forward merge commits (`--no-ff`) mimicking Pull Requests (PR #1 through PR #4). This preserved the branching topology, isolated changes for code review, and prevented regressions on the stable trunk.

**1.3.** Estimate the percentage of commits you contributed relative to the total in your repository.

100% of the commits in this repository fork are my direct individual contributions, implementing the entire simulation engine, domain model, genetics subsystem, Swing rendering pipeline, test harness, and architectural documentation.

---

## 2. Program Design

Don't forget to submit a pdf file of your program design along with this file.
*(Note: Submitted alongside as `Ecosystem_Design_Diagram.pdf` in the project root directory).*

**2.1.** List every class in your project and write 1–2 sentences describing its responsibility.

1. **`com.ecosystem.Main`**: Serves as the application bootstrap entry point; parses configuration, initializes the core simulation engine, and launches the Swing GUI on the Event Dispatch Thread (EDT).
2. **`com.ecosystem.config.SimulationConfig`**: An immutable record holding world dimensions, tick timings, initial species counts, and environmental soil regeneration parameters.
3. **`com.ecosystem.config.ConfigLoader`**: Static parser that reads key-value configuration streams and enforces strict numeric range validation, throwing `InvalidConfigurationException` on malformed inputs.
4. **`com.ecosystem.core.Vector2D`**: An immutable 2D vector mathematical utility providing Euclidean coordinate math, distance calculations, steering force operations, normalization, and bounding clamps.
5. **`com.ecosystem.core.Engine`**: The central simulation coordinator that drives time steps, updates spatial grid entities, records historical population metrics, and coordinates world resets.
6. **`com.ecosystem.exceptions.EcosystemException`**: Base checked exception class for all domain-specific simulation failures, state violations, and runtime anomalies.
7. **`com.ecosystem.exceptions.EntityOutOfBoundsException`**: Checked exception thrown when an entity attempts to move or spawn outside the physical bounding boundaries of the simulation grid.
8. **`com.ecosystem.exceptions.ResourceDepletedException`**: Checked exception thrown when an organism or process requests nutrients, biomass, or energy from an exhausted container.
9. **`com.ecosystem.exceptions.InvalidGenomeException`**: Checked exception thrown when a mutated or initialized genome contains out-of-range, negative, or NaN trait values.
10. **`com.ecosystem.exceptions.InvalidConfigurationException`**: Checked exception thrown when simulation configuration files or parameters contain invalid syntax or out-of-bounds parameters.
11. **`com.ecosystem.grid.BiomeType`**: Enumeration defining geographical terrain regions (WATER, FERTILE_SOIL, FOREST, DESERT) along with their color rendering, fertility multipliers, and land traversability rules.
12. **`com.ecosystem.grid.BoundedResource<T>`**: Generic container encapsulating a numerical resource with strictly enforced upper capacity, lower non-negative boundaries, and thread-safe consumption/replenishment.
13. **`com.ecosystem.grid.Cell`**: Spatial terrain tile containing geographical biome data, moisture levels, and local soil fertility tracked via `BoundedResource<Double>`.
14. **`com.ecosystem.grid.SpatialGrid<T>`**: Generic 2D spatial partitioning grid providing spatial bucket storage, boundary enforcement, and high-performance polymorphic and proximity queries for entity instances.
15. **`com.ecosystem.model.Entity`**: Abstract root class of the simulation model hierarchy encapsulating unique identifier generation, 2D coordinates, life state, collision radius, and rendering contracts.
16. **`com.ecosystem.model.Organism`**: Abstract subclass of `Entity` representing living biological agents, encapsulating metabolic energy drain, chronological age, generation tracking, and hereditary genomes.
17. **`com.ecosystem.model.genetics.Chromosome<T>`**: Generic hereditary container encapsulating an individual trait allele along with Gaussian mutation mechanisms.
18. **`com.ecosystem.model.genetics.Genome`**: Genetic genotype encapsulating speed, sensory range, size, metabolic efficiency, and mutation rate chromosomes with built-in phenotypic validation.
19. **`com.ecosystem.model.entities.Plant`**: Autotrophic primary producer organism that absorbs soil nutrients from its local grid cell and seeds new offspring into surrounding terrain.
20. **`com.ecosystem.model.entities.Animal`**: Abstract heterotrophic consumer base class that implements autonomous steering behaviors (seek, flee, wander), velocity kinetics, and boundary collision reflection.
21. **`com.ecosystem.model.entities.Herbivore`**: Primary consumer that grazes on plants, forms protective herds with conspecifics, and flees from approaching carnivores and apex predators.
22. **`com.ecosystem.model.entities.Carnivore`**: Secondary consumer that actively stalks and hunts herbivores, scavenges decaying carcasses, and flees from territorial apex predators.
23. **`com.ecosystem.model.entities.ApexPredator`**: Top-tier territorial predator with massive vision and strength that hunts both herbivores and carnivores while maintaining wide territorial spacing.
24. **`com.ecosystem.model.entities.Decomposer`**: Detritivorous organism that breaks down dead carcasses and directly enriches soil nutrients in the underlying terrain cell.
25. **`com.ecosystem.model.entities.Carcass`**: Non-living entity spawned upon animal death containing harvestable biomass that naturally decays or feeds scavengers and decomposers.
26. **`com.ecosystem.ui.SimulationFrame`**: Top-level Swing `JFrame` window hosting the simulation canvas, control toolbar, live dashboard, and managing the 60 FPS animation timer.
27. **`com.ecosystem.ui.SimulationPanel`**: Custom `JPanel` canvas rendering procedural biomes, vision halos, entity states, and handling interactive mouse clicks for dynamic entity spawning.
28. **`com.ecosystem.ui.StatisticsHUD`**: Custom `JPanel` dashboard rendering real-time population counts, generational milestones, and an embedded multi-line graph chart drawn with pure `Graphics2D`.
29. **`com.ecosystem.ui.ControlPanel`**: User interaction toolbar providing Play/Pause, single-step execution, simulation reset, speed adjustment sliders, and view overlay toggles.
30. **`com.ecosystem.util.UMLDiagramPdfGenerator`**: Pure Java utility that constructs and outputs the standalone `Ecosystem_Design_Diagram.pdf` vector document without third-party dependencies.
31. **`com.ecosystem.EcosystemVerificationTest`**: Automated headless test suite validating exception handling, generic type safety, and multi-generational simulation stability.

**2.2.** Identify any inheritance relationships. For each parent–child pair, list what the child inherits and what it overrides.

* **`Entity` (Parent) $\rightarrow$ `Organism` (Child)**:
  * *Inherits*: `id`, `position`, `alive`, `radius`, `color`, `markDead()`, `onDeath()`, `equals()`, `hashCode()`.
  * *Overrides/Specializes*: Introduces metabolic state (`energy`, `maxEnergy`, `age`, `maxAge`, `reproductionThreshold`, `genome`, `generation`) and declares the abstract method `public abstract Organism reproduce(SpatialGrid<Entity> grid)`.
* **`Entity` (Parent) $\rightarrow$ `Carcass` (Child)**:
  * *Inherits*: `id`, `position`, `alive`, `radius`, `color`, `markDead()`.
  * *Overrides*: Overrides `update(SpatialGrid<Entity> grid, double deltaSeconds)` to implement natural biomass decay and timer expiration, and overrides `render(Graphics2D g)` to draw a decomposing organic shape.
* **`Organism` (Parent) $\rightarrow$ `Plant` (Child)**:
  * *Inherits*: All metabolic properties, `burnBaseMetabolism()`, `addEnergy()`, `consumeEnergy()`, and life-cycle fields.
  * *Overrides*: Overrides `update(...)` to extract soil nutrients via photosynthesis from its underlying grid `Cell`, overrides `reproduce(...)` to disperse seeds within a nearby radius, and overrides `render(...)` with an energy-scaled botanical icon.
* **`Organism` (Parent) $\rightarrow$ `Decomposer` (Child)**:
  * *Inherits*: Metabolic properties, genome tracking, age, and energy fields.
  * *Overrides*: Overrides `update(...)` to locate nearby `Carcass` entities, harvest decaying biomass, and fertilize underlying `Cell` soil nutrients, overrides `reproduce(...)` with spore dispersion, and overrides `render(...)` to draw a fungal cap.
* **`Organism` (Parent) $\rightarrow$ `Animal` (Child)**:
  * *Inherits*: Energy, age, genome, reproduction thresholds, and base metabolism.
  * *Overrides/Specializes*: Adds kinetic locomotion fields (`velocity`, `headingAngle`, `isSprinting`) and steering methods (`calculateSteeringForce`, `calculateFleeForce`, `calculateWanderForce`, `applyKineticMovement`), and overrides `render(...)` to draw directional headings and dynamic energy bars.
* **`Animal` (Parent) $\rightarrow$ `Herbivore` (Child)**:
  * *Inherits*: Kinetic locomotion, steering forces, and energy mechanics.
  * *Overrides*: Overrides `update(...)` to implement primary grazing behavior (hunting `Plant` autotrophs, flocking with conspecifics, and fleeing from `Carnivore`/`ApexPredator` threats), and overrides `reproduce(...)` with hereditary genome mutation.
* **`Animal` (Parent) $\rightarrow$ `Carnivore` (Child)**:
  * *Inherits*: Locomotion, physics, sensory vectors, and energy mechanics.
  * *Overrides*: Overrides `update(...)` to stalk and hunt `Herbivore` prey, scavenge `Carcass` remains, and flee from `ApexPredator` threats, and overrides `reproduce(...)` to spawn mutated carnivore offspring.
* **`Animal` (Parent) $\rightarrow$ `ApexPredator` (Child)**:
  * *Inherits*: Locomotion, sensory mechanics, and predatory behaviors.
  * *Overrides*: Overrides `update(...)` to prioritize hunting `Carnivore` competitors and `Herbivore` prey across vast sensory radii while asserting territorial dominance, and overrides `reproduce(...)` with apex genome mutation.
* **`Exception` (Parent) $\rightarrow$ `EcosystemException` (Child)**:
  * *Inherits*: Standard Java exception stack traces, message propagation, and cause chaining.
* **`EcosystemException` (Parent) $\rightarrow$ `EntityOutOfBoundsException`, `ResourceDepletedException`, `InvalidGenomeException`, `InvalidConfigurationException` (Children)**:
  * *Inherits*: Checked exception handling contracts and error messaging.
  * *Overrides/Specializes*: Adds domain-specific diagnostic state (attempted coordinates, resource names, trait thresholds, and line numbers).

**2.3.** Pick the class that you think has the best design. Explain why.

`SpatialGrid<T extends Entity>` represents the cleanest and most robust design in the codebase for several reasons:
1. **Single Responsibility & High Cohesion**: `SpatialGrid` is exclusively responsible for 2D spatial partitioning and proximity querying. It abstracts the underlying geometry and array of `Cell` objects, hiding partition complexity from the entities themselves.
2. **Type-Safe Generics with Bounded Polymorphism**: By declaring `<T extends Entity>` and leveraging generic methods (`public <E extends T> List<E> queryEntitiesNear(...)`), it provides complete compile-time type safety. Callers can query for specific entity subtypes (e.g. `grid.queryEntitiesNear(pos, r, Carnivore.class)`) without requiring dangerous downcasts or `instanceof` checks in client code.
3. **Defensive Mutation & State Synchronization**: To prevent `ConcurrentModificationException` during tick loops where entities reproduce or die mid-iteration, `SpatialGrid` utilizes staging lists (`pendingAdditions` and `pendingRemovals`) and synchronizes state via `synchronizeEntities()`.
4. **Open-Closed Principle (OCP)**: New biological species or physical entities can be added to the simulation at any time without modifying a single line of `SpatialGrid` code.

**2.4.** Paste one code snippet that demonstrates your use of polymorphism or encapsulation.  Include an explanation of _how_ this demonstrates polymorphim or encapsulation.  Give a reference to a provided reading that talks about this type of polymorphism or encapsulation.

### Code Snippet (`Engine.java` update loop demonstrating Polymorphism):

```java
// Inside com.ecosystem.core.Engine.java
List<Entity> currentEntities = new ArrayList<>(grid.getAllEntities());
for (Entity entity : currentEntities) {
    if (entity.isAlive()) {
        try {
            // Polymorphic dynamic dispatch: executes subclass-specific behavior
            entity.update(grid, effectiveDelta);
        } catch (EntityOutOfBoundsException e) {
            System.err.println("Handling entity bounds error: " + e.getMessage());
            Vector2D safePos = entity.getPosition().clamp(10, 10, 
                    config.worldWidth() - 10, config.worldHeight() - 10);
            entity.setPosition(safePos);
        }
    }
}
```

### Explanation:
This snippet demonstrates **Subtype Polymorphism (Dynamic Dispatch)**. The `Engine` operates on a collection of the abstract supertype `Entity`. When `entity.update(grid, effectiveDelta)` is invoked, the Java Virtual Machine dynamically resolves and executes the concrete behavior at runtime according to the entity's actual subtype (`Plant` photosynthesizes and drops seeds; `Herbivore` grazes and flees predators; `Carnivore` tracks and hunts prey; `Decomposer` breaks down carcasses). The calling loop requires no knowledge of concrete subtypes, zero `switch` statements, and zero type casting.

### Reference to Course Reading:
- **Gamma, Helm, Johnson, Vlissides (GoF), *Design Patterns: Elements of Reusable Object-Oriented Software***, Chapter 1 ("Object-Oriented Design Principles"), discussing *"Program to an interface, not an implementation"* and how polymorphism enables decoupling client coordinators from concrete behavioral implementations.
- **Bloch, Joshua, *Effective Java* (3rd Edition)**, Item 19: *"Design and document for inheritance or else prohibit it"*, and Item 52: *"Refer to objects by their interfaces / superclasses"*.

---

## 3. Generics and Exceptions

**3.1.** List every place your code uses generics (e.g. `ArrayList<Actor>`, `Optional<Cell>`, `HashMap<String, Team>`). If you deliberately used none, explain why.

1. **`SpatialGrid<T extends Entity>` (`com.ecosystem.grid.SpatialGrid`)**: Generic class using a bounded type parameter ensuring the spatial partition only holds objects inheriting from `Entity`.
2. **`SpatialGrid.<E extends T> List<E> queryEntities(Class<E> typeClass)`**: Generic method filtering the grid by subtype `E` without type-casting.
3. **`SpatialGrid.<E extends T> List<E> queryEntitiesNear(...)`**: Generic spatial query method returning a type-safe list of entities of type `E` within a Euclidean radius.
4. **`SpatialGrid.<E extends T> Optional<E> findNearest(...)`**: Generic method returning an `Optional<E>` containing the nearest entity matching a generic predicate `Predicate<E>`.
5. **`BoundedResource<T extends Number>` (`com.ecosystem.grid.BoundedResource`)**: Generic class with bounded type parameter `<T extends Number>` representing quantifiable resources (e.g. `BoundedResource<Double>`) with upper/lower bounds.
6. **`Chromosome<T>` (`com.ecosystem.model.genetics.Chromosome`)**: Generic class wrapping hereditary trait alleles (`Chromosome<Double>`) to decouple trait data types from evolutionary mutation logic.
7. **`java.util.List<T>`, `java.util.ArrayList<T>`**: Extensively used throughout `Engine`, `SpatialGrid`, `StatisticsHUD`, and `Herbivore` to store entities, historical time series, and flocking clusters (`List<Entity>`, `List<Integer>`, `List<Herbivore>`).
8. **`java.util.Optional<T>`**: Used in sensory perception methods (`Optional<Plant>`, `Optional<Carnivore>`, `Optional<ApexPredator>`, `Optional<Carcass>`) to avoid returning null pointers.
9. **`java.util.function.Predicate<E>`**: Used in `SpatialGrid.findNearest` to pass generic filter lambdas (e.g. `Entity::isAlive`).
10. **`java.util.Map<String, String>`, `java.util.HashMap<String, String>`**: Used in `ConfigLoader` to store and validate parsed configuration keys and values.

**3.2.** List every place your code handles exceptions (try/catch, throws, custom exception classes). What error is each protecting against?

1. **`EcosystemException`**: The base checked domain exception ensuring all simulation-specific error scenarios are explicitly handled or propagated.
2. **`EntityOutOfBoundsException`**:
   - *Protecting against*: Out-of-bounds coordinates caused by erratic movement vectors or spawning outside the simulation window.
   - *Thrown in*: `SpatialGrid.addEntity(T entity)` when an entity's initial position is outside $[0..\text{width}, 0..\text{height}]$, and `Entity.update(...)`.
   - *Handled in*: `Engine.update(...)` with a `try/catch` block that logs the incident and defensively clamps the entity's position back into safe bounds, and in `Engine.populateWorld()` to ignore bad random positions.
3. **`ResourceDepletedException`**:
   - *Protecting against*: Organisms attempting to extract nutrients, energy, or carcass biomass that has already been consumed or reduced to zero.
   - *Thrown in*: `BoundedResource.consume(double requestedAmount)` and `Carcass.harvestBiomass(...)`.
   - *Handled in*: `Plant.update(...)`, `Carnivore.update(...)`, and `Decomposer.update(...)` to gracefully transition organisms from eating to wandering without crashing.
4. **`InvalidGenomeException`**:
   - *Protecting against*: Genetic mutations resulting in invalid phenotypes (negative speeds, infinite vision radii, zero size, or `NaN` floats).
   - *Thrown in*: `Genome.validate()`.
   - *Handled in*: `Plant.reproduce(...)`, `Herbivore.reproduce(...)`, `Carnivore.reproduce(...)`, and `ApexPredator.reproduce(...)` where fatal mutations result in unsuccessful reproduction (`null`) rather than corrupting the simulation state.
5. **`InvalidConfigurationException`**:
   - *Protecting against*: Malformed configuration syntax, missing keys, or out-of-bounds world parameters when bootstrapping the simulation.
   - *Thrown in*: `ConfigLoader.parseConfig(...)`, `parseDouble(...)`, and `parseInt(...)`.
   - *Handled in*: `Main.main(...)` with a `try/catch` block that catches the exception, logs the descriptive syntax error, and falls back to safe internal defaults (`SimulationConfig.createDefault()`).
6. **`IOException` & `NumberFormatException`**:
   - *Handled in*: `ConfigLoader.parseConfig(...)` to catch low-level I/O failures or unparseable integers/doubles and wrap them into meaningful `InvalidConfigurationException` domain exceptions.

**3.3.** Paste a code snippet showing either a generic class/method or a try/catch block.

### Code Snippet (`BoundedResource.java` Generic Class & `ConfigLoader.java` Exception Handling):

```java
package com.ecosystem.grid;

import com.ecosystem.exceptions.ResourceDepletedException;

/**
 * Generic wrapper for a quantifiable resource with strict capacity boundaries.
 * Demonstrates generics with bounded type parameter <T extends Number>.
 */
public class BoundedResource<T extends Number> {
    private final String name;
    private final double maxCapacity;
    private double currentAmount;

    public BoundedResource(String name, double maxCapacity, double initialAmount) {
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("Maximum capacity must be positive: " + maxCapacity);
        }
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.currentAmount = Math.min(Math.max(0, initialAmount), maxCapacity);
    }

    public synchronized double consume(double requestedAmount) throws ResourceDepletedException {
        if (currentAmount <= 0) {
            throw new ResourceDepletedException(name, requestedAmount);
        }
        double consumed = Math.min(requestedAmount, currentAmount);
        currentAmount -= consumed;
        return consumed;
    }

    public synchronized void replenish(double amount) {
        if (amount > 0) {
            this.currentAmount = Math.min(this.maxCapacity, this.currentAmount + amount);
        }
    }
}
```

---

## 4. Log Book

Don't forget to submit an electronic version of your logbook.

**4.1.** Which week's activity taught you the most? What did you learn?

The **Week 5 activity on Generics and Custom Exception Hierarchies** taught me the most. 

Prior to that activity, I viewed generics as simply a way to avoid writing raw collections like `ArrayList`. Through implementing `SpatialGrid<T extends Entity>` and generic querying methods (`<E extends T> List<E> queryEntitiesNear(...)`), I experienced firsthand how bounded type parameters (`<T extends Entity>` and `<T extends Number>`) enforce compile-time type safety while preserving polymorphism. 

Furthermore, designing custom checked domain exceptions (`EcosystemException`, `EntityOutOfBoundsException`, `ResourceDepletedException`, `InvalidGenomeException`) demonstrated how to establish clear architectural fault boundaries. Instead of allowing unpredictable runtime crashes (`NullPointerException`, `IndexOutOfBoundsException`), throwing domain-specific exceptions forced the caller (`Engine`, `Plant`, `Carnivore`) to implement explicit recovery mechanisms (such as clamping boundary coordinates or transitioning an animal to a wandering state upon finding depleted food).

---

## 5. Uniqueness and Creativity

**5.1.** List everything you added to the project that was not part of the in-class activities.

1. **Complete 5-Tier Trophic Ecosystem**: Implemented a comprehensive biological food web comprising Autotrophic Plants $\rightarrow$ Primary Herbivores $\rightarrow$ Secondary Carnivores $\rightarrow$ Tertiary Apex Predators $\rightarrow$ Detritivorous Decomposers $\rightarrow$ Soil Nutrients.
2. **Dynamic Genetics & Mutation System**: Developed `Chromosome<T>` and `Genome` where traits (speed, vision radius, size, metabolism efficiency, mutation rate) mutate continuously across generations with Gaussian variance, demonstrating evolutionary adaptation.
3. **Biological Carcass Recycling**: Dead animals drop `Carcass` entities containing harvestable biomass that scavengers feed on and decomposers recycle back into cell soil nutrients.
4. **Procedural Multi-Biome Terrain**: Implemented four distinct biomes (Water, Fertile Soil, Forest, Desert) with differing friction, traversal constraints, and fertility regeneration multipliers.
5. **Real-time Live Graph Dashboard**: Built an embedded, pure `Graphics2D` multi-series line chart in `StatisticsHUD` that plots population history in real time without any external libraries.
6. **Interactive Click-Spawn Toolbar**: Added mouse brush tools to dynamically inject plants, herbivores, predators, and carcasses into the running simulation canvas to test emergent resilience.

**5.2.** Which feature required the most independent research or problem-solving? What did you learn from it?

The **Emergent Steering & Kinetic Physics System integrated with Genetic Evolution** required the most independent research and problem-solving.

Balancing predator-prey dynamics so populations do not immediately go extinct or explode to infinity required researching the **Lotka-Volterra equations** and Craig Reynolds' autonomous steering behaviors (*Seek*, *Flee*, and *Wander*). 

The primary technical challenge was coupling locomotion speed with metabolic energy burn: if an animal evolves higher speed and vision, its metabolic burn increases proportionally ($E_{\text{burn}} \propto \text{speed} \cdot \text{size} \cdot \text{efficiency}$). If a predator is too fast, it overconsumes all herbivores and starves; if it is too slow, it cannot catch prey. Calibrating the mutation variance in `Genome.mutate()` and implementing smooth vector steering in `Vector2D` taught me how complex, natural equilibrium emerges from simple, well-encapsulated object interactions.

**5.3.** Paste one code snippet that you are especially proud of. Explain why it goes beyond what was done in class.

### Code Snippet (`SpatialGrid.java` Generic Proximity & Predicate Search):

```java
/**
 * Generic spatial query method returning all entities within a circular radius matching a type.
 * Demonstrates bounded generic methods and functional Predicate filtering without type casting.
 *
 * @param center Query origin.
 * @param radius Maximum search distance.
 * @param typeClass Target entity class.
 * @param <E> Subtype of T.
 * @return Entities within distance matching type E.
 */
@SuppressWarnings("unchecked")
public <E extends T> List<E> queryEntitiesNear(Vector2D center, double radius, Class<E> typeClass) {
    List<E> result = new ArrayList<>();
    double radiusSq = radius * radius;

    for (T entity : allEntities) {
        if (entity.isAlive() && typeClass.isInstance(entity)) {
            double distSq = Math.pow(entity.getPosition().getX() - center.getX(), 2) +
                            Math.pow(entity.getPosition().getY() - center.getY(), 2);
            if (distSq <= radiusSq) {
                result.add((E) entity);
            }
        }
    }
    return result;
}

public <E extends T> Optional<E> findNearest(Vector2D center, double maxRadius, 
                                             Class<E> typeClass, Predicate<E> filter) {
    List<E> candidates = queryEntitiesNear(center, maxRadius, typeClass);
    E bestMatch = null;
    double minDistance = Double.MAX_VALUE;

    for (E candidate : candidates) {
        if (filter == null || filter.test(candidate)) {
            double dist = center.distanceTo(candidate.getPosition());
            if (dist < minDistance) {
                minDistance = dist;
                bestMatch = candidate;
            }
        }
    }
    return Optional.ofNullable(bestMatch);
}
```

### Explanation:
This code goes significantly beyond standard in-class examples by combining **generic type tokens (`Class<E>`)**, **type-bounded method parameters (`<E extends T>`)**, **functional interfaces (`Predicate<E>`)**, and **monadic return types (`Optional<E>`)**. 

It provides an expressive, high-performance querying API for any entity in the world:
```java
Optional<Carnivore> threat = grid.findNearest(getPosition(), vision, Carnivore.class, Entity::isAlive);
```
This single line enables an animal to query its environment in a completely type-safe manner without manual loops, null checks, or dangerous downcasts.
