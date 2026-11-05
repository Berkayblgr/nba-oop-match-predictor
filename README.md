# NBA OOP Analyzer & Match Predictor

A zero-dependency Java console application that fetches live NBA rosters, coaching staff, injury reports, and per-game player statistics from ESPN's public endpoints to analyze player impact and simulate matchup win probabilities.

Built with pure Java (`java.net.http.HttpClient`, `CompletableFuture`, custom JSON parsing, and core Object-Oriented Programming patterns) without external libraries or build frameworks.

---

## Key Architecture & OOP Principles

- **Abstraction & Inheritance (`nba.model`):** Abstract base class `Person` encapsulates shared state and validation (`name`, `age`), extended by `Player` and `Coach` with polymorphic `getRoleSummary()` implementations.
- **Encapsulation & Defensive Copying:** Strict input validation on all constructors/setters, immutable `MatchPredictionResult` value objects, and unmodifiable roster views via `List.copyOf(...)`.
- **Custom Iterator Pattern (`nba.iterator`):** `Team` implements `Iterable<Player>` backed by a custom `PlayerIterator` (`Iterator<Player>`), enabling idiomatic `for (Player player : team)` traversal without exposing internal list references.
- **Strategy Pattern & Polymorphism (`nba.prediction`):** `Predictable` defines a common contract implemented by `RegularSeasonPredictor` and `PlayoffPredictor`, selected dynamically at runtime in `ConsoleMenu`.
- **Asynchronous Networking & Resilience (`nba.network`):** `NbaApiClient` dispatches non-blocking per-athlete HTTP requests concurrently via `CompletableFuture`, caches loaded `Team` instances in memory, and falls back gracefully to offline rosters if network connectivity is unavailable.

```mermaid
classDiagram
    class Person {
        <<abstract>>
        -String name
        -int age
        +getRoleSummary()* String
    }
    class Player {
        -String position
        -double ppg
        -double rpg
        -double apg
        -double spg
        -double bpg
        -double topg
        -double fgPct
        +calculateImpactScore() double
        +getRoleSummary() String
    }
    class Coach {
        -String tacticalStyle
        -double tacticalBonus
        +getRoleSummary() String
    }
    class Team {
        -String name
        -String abbreviation
        -Coach coach
        -List~Player~ roster
        -int wins
        -int losses
        +iterator() Iterator~Player~
        +calculateTeamPower() double
    }
    class PlayerIterator {
        -List~Player~ players
        -int currentIndex
        +hasNext() boolean
        +next() Player
    }
    class Predictable {
        <<interface>>
        +getPredictorName() String
        +predictWinner(Team, Team) MatchPredictionResult
    }
    class RegularSeasonPredictor {
        +predictWinner(Team, Team) MatchPredictionResult
    }
    class PlayoffPredictor {
        +predictWinner(Team, Team) MatchPredictionResult
    }
    class MatchPredictionResult {
        -Team predictedWinner
        -double winProbability
        -String analysisSummary
    }

    Person <|-- Player
    Person <|-- Coach
    Team *-- Coach
    Team o-- Player
    Team ..> PlayerIterator : creates
    Predictable <|.. RegularSeasonPredictor
    Predictable <|.. PlayoffPredictor
    Predictable ..> MatchPredictionResult : returns
```

---

## Prediction Model

1. **Player Impact Score & Shooting Efficiency (`FG%`):** Weighted box-score metrics scaled by an efficiency multiplier relative to the NBA league average (46.0%):
   - `BaseImpact = 1.0*PPG + 1.2*RPG + 1.5*APG + 2.0*SPG + 1.6*BPG - 1.8*TOPG`
   - `Multiplier = 1.0 + ((FG% - 46.0) / 200.0)`
   - `ImpactScore = BaseImpact * Multiplier`
2. **Live Injury Filtering:** Players marked `"status":"Out"` in ESPN's live roster feed are excluded automatically before rating calculation.
3. **Top-8 Rotation & Team Form:** Rather than diluting team strength with end-of-bench garbage-time minutes, `Team.calculateTeamPower()` evaluates the top 8 active rotation players (`1.2x` weight for starters 1–5, `0.6x` weight for bench rotation 6–8), adds the head coach's tactical bonus, and incorporates a live win-percentage form factor `(winPct - 0.50) * 14.0`.
4. **Context-Specific Modes & Pythagorean Win Expectancy:**
   - **Regular Season:** Applies a standard `+2.2` home-court rating bonus.
   - **Playoffs:** Amplifies the team's #1 star (`+0.35 * maxStarImpact`), coaching adjustments (`+0.50 * tacticalBonus`), and home-court intensity (`+3.2`).
   - Win probability is derived via a Pythagorean exponent (`rating^2.8`) to produce realistic separation between contenders and rebuilding teams.

---

## Sample Console Output

```text
=== NBA 2026-27 OOP ANALYZER & MATCH PREDICTOR ===
[1] List Supported NBA Teams
[2] Inspect Team Roster & Coach
[3] Player Impact Score Ranking
[4] Simulate Match Prediction
[0] Exit
Your choice: 4
Enter home team code to predict (e.g. LAL, BOS, HOU): SAC
Loading live 2026-27 roster for SAC...
Enter away team code to predict (e.g. LAL, BOS, HOU): LAL
Loading live 2026-27 roster for LAL...
Select Mode ([1] Regular Season, [2] Playoff): 1

=== REGULAR SEASON PREDICTOR ===
Predicted Winner : Los Angeles Lakers [LAL] (Win Probability: 65.4%)
Power Breakdown  : SAC (Home, +2.2 Court Bonus): 36.12 rating vs LAL (Away): 45.38 rating
```

---

## Project Structure

```text
src/
├── Main.java
└── nba/
    ├── iterator/
    │   └── PlayerIterator.java
    ├── model/
    │   ├── Person.java
    │   ├── Player.java
    │   ├── Coach.java
    │   └── Team.java
    ├── network/
    │   ├── NbaApiClient.java
    │   └── SimpleJsonParser.java
    ├── prediction/
    │   ├── Predictable.java
    │   ├── MatchPredictionResult.java
    │   ├── RegularSeasonPredictor.java
    │   └── PlayoffPredictor.java
    └── ui/
        └── ConsoleMenu.java
```

---

## Getting Started

### Prerequisites
- **Java JDK 21+** (developed and tested on JDK 25)
- Internet connection for live ESPN NBA stats (automatically falls back to offline rosters if offline)

### Run in IntelliJ IDEA
1. Open the project folder in IntelliJ IDEA.
2. Open `src/Main.java` and click **Run (`▶`)**.

### Run via Terminal
```bash
javac -d out src/Main.java src/nba/iterator/*.java src/nba/model/*.java src/nba/network/*.java src/nba/prediction/*.java src/nba/ui/*.java
java -cp out Main
```
