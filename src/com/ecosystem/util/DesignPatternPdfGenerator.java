package com.ecosystem.util;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure Java Vector PDF Generator for the official COMP2000 Week 13 Design
 * Patterns Diagram.
 * Conforms strictly to standard JRE constraints (zero external dependencies).
 */
public class DesignPatternPdfGenerator {

    public static void main(String[] args) {
        String outputPath = args.length > 0 ? args[0] : "Week 13 Submission/Design Patterns Diagram.pdf";
        try {
            generatePdf(outputPath);
            System.out.println("Successfully generated Design Patterns Diagram PDF at: " + outputPath);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void generatePdf(String filePath) throws IOException {
        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        List<Long> offsets = new ArrayList<>();

        // Header
        write(pdfStream, "%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n");

        // Object 1: Catalog
        offsets.add((long) pdfStream.size());
        write(pdfStream, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        // Object 2: Pages (A3 Landscape: 1191 x 842 pt)
        offsets.add((long) pdfStream.size());
        write(pdfStream, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

        // Object 4: Font Helvetica
        offsets.add((long) pdfStream.size());
        write(pdfStream, "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

        // Object 5: Font Helvetica-Bold
        offsets.add((long) pdfStream.size());
        write(pdfStream, "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n");

        // Object 6: Font Helvetica-Oblique
        offsets.add((long) pdfStream.size());
        write(pdfStream, "6 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Oblique >>\nendobj\n");

        // Build Content Stream
        StringBuilder stream = new StringBuilder();
        buildPdfGraphics(stream);
        byte[] streamBytes = stream.toString().getBytes(StandardCharsets.ISO_8859_1);

        // Object 7: Content Stream
        offsets.add((long) pdfStream.size());
        write(pdfStream, "7 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n");
        pdfStream.write(streamBytes);
        write(pdfStream, "\nendstream\nendobj\n");

        // Object 3: Page Definition
        offsets.add((long) pdfStream.size());
        write(pdfStream, "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 1191 842] " +
                "/Resources << /Font << /F1 4 0 R /F2 5 0 R /F3 6 0 R >> >> /Contents 7 0 R >>\nendobj\n");

        // Cross-Reference Table
        long xrefStart = pdfStream.size();
        write(pdfStream, "xref\n0 " + (offsets.size() + 1) + "\n0000000000 65535 f \n");
        for (Long off : offsets) {
            write(pdfStream, String.format("%010d 00000 n \n", off));
        }

        // Trailer
        write(pdfStream,
                "trailer\n<< /Size " + (offsets.size() + 1) + " /Root 1 0 R >>\nstartxref\n" + xrefStart + "\n%%EOF\n");

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            pdfStream.writeTo(fos);
        }
    }

    private static void write(ByteArrayOutputStream os, String text) throws IOException {
        os.write(text.getBytes(StandardCharsets.ISO_8859_1));
    }

    private static void buildPdfGraphics(StringBuilder sb) {
        // Background fill
        rect(sb, 0, 0, 1191, 842, 0.97f, 0.98f, 0.99f, true);

        // Header Title Banner
        rect(sb, 20, 770, 1151, 55, 0.12f, 0.16f, 0.22f, true);
        text(sb, 35, 804, "/F2", 18, 1f, 1f, 1f, "COMP2000: Object Oriented Programming Practices - Final Submission");
        text(sb, 35, 782, "/F1", 12, 0.75f, 0.85f, 0.95f,
                "Design Patterns & Concurrency Architecture Diagram | Student: Zahin Rafid (47867200)");

        // 1. STATE PATTERN SECTION (Top Left: X: 25, Y: 430, W: 360, H: 325)
        packageBox(sb, 25, 430, 360, 325, "1. BEHAVIOURAL: STATE PATTERN (Animal Life-Cycle)");
        drawUmlClass(sb, 35, 620, 160, 115, "Context", "Animal", "#ede9fe",
                new String[] { "- currentState : AnimalState", "- velocity : Vector2D", "- energy : double" },
                new String[] { "+ update(grid, dt) : void", "+ changeState(s) : void" });
        drawUmlClass(sb, 215, 620, 160, 115, "<<interface>>", "AnimalState", "#ecfdf5",
                new String[] {},
                new String[] { "+ update(animal, grid, dt)*", "+ getStateName()* : String",
                        "+ getStatusColor()* : Color" });
        // Concrete States
        drawUmlClass(sb, 35, 445, 100, 75, "State", "WanderingState", "#ecfdf5", new String[] {},
                new String[] { "+ update()", "+ getStateName()" });
        drawUmlClass(sb, 145, 445, 105, 75, "State", "ForagingState", "#ecfdf5", new String[] { "- food : Entity" },
                new String[] { "+ update()", "+ getStateName()" });
        drawUmlClass(sb, 260, 445, 115, 75, "State", "FleeingState", "#ecfdf5", new String[] { "- threat : Entity" },
                new String[] { "+ update()", "+ getStateName()" });

        // Association line Context -> State
        line(sb, 195, 675, 215, 675, 0.2f, 0.4f, 0.8f);

        // State description annotation
        text(sb, 35, 545, "/F1", 8, 0.3f, 0.35f, 0.4f,
                "Context delegates update() to active state; states trigger transitions");
        text(sb, 35, 532, "/F1", 8, 0.3f, 0.35f, 0.4f,
                "(e.g., threat detected -> FleeingState; hunger -> ForagingState).");

        // 2. STRATEGY PATTERN SECTION (Top Right: X: 405, Y: 430, W: 375, H: 325)
        packageBox(sb, 405, 430, 375, 325, "2. BEHAVIOURAL: STRATEGY PATTERN (Steering Algorithms)");
        drawUmlClass(sb, 415, 620, 165, 115, "Context", "Animal", "#ede9fe",
                new String[] { "- movementStrategy : MovementStrategy" },
                new String[] { "+ setMovementStrategy(s)", "+ calculateSteeringForce()" });
        drawUmlClass(sb, 605, 620, 165, 115, "<<functional interface>>", "MovementStrategy", "#fef3c7",
                new String[] {},
                new String[] { "+ calculateSteering(a, g, dt)*" });
        // Concrete Strategies & Lambdas
        drawUmlClass(sb, 415, 445, 115, 75, "Strategy", "FlockingStrategy", "#fef3c7",
                new String[] { "- radius : double" }, new String[] { "+ calculateSteering()" });
        drawUmlClass(sb, 540, 445, 110, 75, "Strategy", "AmbushStrategy", "#fef3c7", new String[] {},
                new String[] { "+ calculateSteering()" });
        drawUmlClass(sb, 660, 445, 110, 75, "Strategy", "WanderStrategy", "#fef3c7",
                new String[] { "- angle : double" }, new String[] { "+ calculateSteering()" });

        line(sb, 580, 675, 605, 675, 0.8f, 0.5f, 0.1f);
        text(sb, 415, 545, "/F1", 8, 0.3f, 0.35f, 0.4f,
                "Interchangeable algorithms selectable at runtime or via lambdas:");
        text(sb, 415, 532, "/F1", 8, 0.3f, 0.35f, 0.4f,
                "herbivore.setMovementStrategy((a, g, dt) -> a.calculateWanderForce());");

        // 3. OBSERVER PATTERN SECTION (Top Right Corner: X: 800, Y: 430, W: 365, H:
        // 325)
        packageBox(sb, 800, 430, 365, 325, "3. BEHAVIOURAL: OBSERVER / PUB-SUB PATTERN (Event Bus)");
        drawUmlClass(sb, 810, 620, 165, 115, "Subject / Broker", "EcosystemEventBus", "#dbeafe",
                new String[] { "- listeners : List<EcosystemListener>" },
                new String[] { "+ subscribe(l) : void", "+ publish(event) : void" });
        drawUmlClass(sb, 990, 620, 165, 115, "<<functional interface>>", "EcosystemListener", "#dbeafe",
                new String[] {},
                new String[] { "+ onEcosystemEvent(event)*" });
        drawUmlClass(sb, 810, 445, 165, 75, "Concrete Observer", "StatisticsHUD", "#dbeafe",
                new String[] { "- recentEventLogs : List<String>" },
                new String[] { "+ handleEcosystemEvent(e)" });
        drawUmlClass(sb, 990, 445, 165, 75, "Immutable Record", "EcosystemEvent", "#dbeafe",
                new String[] { "- type : EcosystemEventType", "- message : String", "- tick : long" },
                new String[] { "+ of(type, msg, pos, t, src)" });

        line(sb, 975, 675, 990, 675, 0.2f, 0.5f, 0.8f);
        text(sb, 810, 545, "/F1", 8, 0.3f, 0.35f, 0.4f,
                "Decoupled 1-to-many event notification: Engine dispatches births,");
        text(sb, 810, 532, "/F1", 8, 0.3f, 0.35f, 0.4f, "predation, and disasters without referencing UI components.");

        // 4. DECORATOR PATTERN SECTION (Bottom Left: X: 25, Y: 45, W: 450, H: 365)
        packageBox(sb, 25, 45, 450, 365, "4. STRUCTURAL: DECORATOR PATTERN (Dynamic Status Effects & Buffs)");
        drawUmlClass(sb, 35, 230, 195, 145, "<<abstract>> Component", "Organism", "#fef2f2",
                new String[] { "- energy, maxEnergy : double", "- age, maxAge : double", "- genome : Genome" },
                new String[] { "+ update(grid, dt)* : void", "+ render(g)* : void", "+ reproduce(grid)* : Organism" });
        drawUmlClass(sb, 260, 230, 205, 145, "<<abstract>> Decorator", "OrganismDecorator", "#fee2e2",
                new String[] { "# wrapped : Organism" },
                new String[] { "+ update(grid, dt) : void", "+ render(g) : void", "+ getEnergy() : double" });
        drawUmlClass(sb, 35, 65, 130, 95, "ConcreteDecorator", "InfectedDebuff", "#fee2e2",
                new String[] { "- drainRate : double" },
                new String[] { "+ update(grid, dt)", "+ render(g)" });
        drawUmlClass(sb, 175, 65, 140, 95, "ConcreteDecorator", "CamouflageBuff", "#fee2e2",
                new String[] { "- concealment : double" },
                new String[] { "+ update(grid, dt)", "+ render(g)" });
        drawUmlClass(sb, 325, 65, 140, 95, "ConcreteDecorator", "AdrenalineBuff", "#fee2e2",
                new String[] { "- duration : double" },
                new String[] { "+ isExpired() : boolean", "+ update(grid, dt)" });
        line(sb, 230, 300, 260, 300, 0.8f, 0.2f, 0.2f);
        text(sb, 35, 185, "/F1", 8, 0.3f, 0.35f, 0.4f,
                "Wraps organisms dynamically at runtime to attach infection debuffs, camouflage,");
        text(sb, 35, 172, "/F1", 8, 0.3f, 0.35f, 0.4f,
                "or adrenaline surges without subclass explosion (Open/Closed Principle).");

        // 5. STREAMS, PARALLELISM & CUSTOM COLLECTOR (Bottom Right: X: 495, Y: 45, W:
        // 670, H: 365)
        packageBox(sb, 495, 45, 670, 365, "5. FUNCTIONAL PROGRAMMING & PARALLEL STREAMS ARCHITECTURE");

        drawUmlClass(sb, 505, 230, 290, 145, "Utility Class", "EcosystemCollectors", "#ecfdf5",
                new String[] { "- toEcosystemSummary() : Collector<Entity, ?, EcosystemSummary>" },
                new String[] { "+ Accumulator.accumulate(entity)", "+ Accumulator.merge(other)",
                        "+ Accumulator.finish() : EcosystemSummary" });
        drawUmlClass(sb, 815, 230, 340, 145, "Immutable Record", "EcosystemSummary", "#ecfdf5",
                new String[] { "- speciesCounts : Map<String, Long>", "- totalBiomass : double",
                        "- shannonDiversityIndex : double", "- averageSpeed, averageVision : double" },
                new String[] { "+ shannonDiversityIndex() : double", "+ totalOrganisms() : long" });

        // Pipeline Flow Box
        rect(sb, 505, 65, 650, 125, 0.94f, 0.96f, 0.98f, true);
        rect(sb, 505, 65, 650, 125, 0.75f, 0.8f, 0.85f, false);
        text(sb, 515, 172, "/F2", 10, 0.1f, 0.3f, 0.6f,
                "Thread-Safe Parallel Stream Pipeline Execution (Engine.java):");
        text(sb, 515, 153, "/F1", 9, 0.15f, 0.2f, 0.25f,
                "EcosystemSummary summary = grid.getAllEntitiesSnapshot().parallelStream()");
        text(sb, 555, 138, "/F1", 9, 0.15f, 0.2f, 0.25f, ".filter(Entity::isAlive)");
        text(sb, 555, 123, "/F1", 9, 0.15f, 0.2f, 0.25f, ".collect(EcosystemCollectors.toEcosystemSummary());");
        text(sb, 515, 103, "/F2", 9, 0.05f, 0.45f, 0.25f, "Thread-Safety Mechanisms:");
        text(sb, 515, 90, "/F1", 8, 0.3f, 0.35f, 0.4f,
                "* Read Isolation: Operates on immutable snapshot; write mutations buffered in ConcurrentLinkedQueue.");
        text(sb, 515, 77, "/F1", 8, 0.3f, 0.35f, 0.4f,
                "* Associative Combiner: Accumulator.merge() guarantees deterministic, lock-free parallel reduction.");
    }

    private static void packageBox(StringBuilder sb, int x, int y, int w, int h, String title) {
        rect(sb, x, y, w, h, 1f, 1f, 1f, true);
        rect(sb, x, y, w, h, 0.82f, 0.85f, 0.88f, false);
        text(sb, x + 8, y + h - 14, "/F2", 9, 0.2f, 0.45f, 0.8f, title);
        line(sb, x, y + h - 20, x + w, y + h - 20, 0.82f, 0.85f, 0.88f);
    }

    private static void drawUmlClass(StringBuilder sb, int x, int y, int w, int h, String stereotype, String name,
            String headerBg, String[] fields, String[] methods) {
        rect(sb, x, y, w, h, 1f, 1f, 1f, true);
        rect(sb, x, y, w, h, 0.25f, 0.3f, 0.35f, false);

        int headerH = stereotype.isEmpty() ? 20 : 28;
        float hr = headerBg.equals("#ede9fe") ? 0.93f
                : (headerBg.equals("#ecfdf5") ? 0.92f
                        : (headerBg.equals("#fee2e2") ? 0.99f : (headerBg.equals("#fef3c7") ? 0.99f : 0.95f)));
        float hg = headerBg.equals("#ede9fe") ? 0.91f
                : (headerBg.equals("#ecfdf5") ? 0.99f
                        : (headerBg.equals("#fee2e2") ? 0.92f : (headerBg.equals("#fef3c7") ? 0.97f : 0.96f)));
        float hb = headerBg.equals("#ede9fe") ? 0.99f
                : (headerBg.equals("#ecfdf5") ? 0.96f
                        : (headerBg.equals("#fee2e2") ? 0.92f : (headerBg.equals("#fef3c7") ? 0.85f : 0.97f)));

        rect(sb, x, y + h - headerH, w, headerH, hr, hg, hb, true);
        line(sb, x, y + h - headerH, x + w, y + h - headerH, 0.25f, 0.3f, 0.35f);

        if (!stereotype.isEmpty()) {
            text(sb, x + 6, y + h - 10, "/F3", 7, 0.45f, 0.5f, 0.55f, stereotype);
            text(sb, x + 6, y + h - 22, "/F2", 9, 0.05f, 0.1f, 0.15f, name);
        } else {
            text(sb, x + 6, y + h - 14, "/F2", 9, 0.05f, 0.1f, 0.15f, name);
        }

        int curY = y + h - headerH - 11;
        for (String field : fields) {
            text(sb, x + 5, curY, "/F1", 7, 0.2f, 0.25f, 0.3f, field);
            curY -= 10;
        }

        if (fields.length > 0 && methods.length > 0) {
            line(sb, x, curY + 3, x + w, curY + 3, 0.85f, 0.88f, 0.92f);
            curY -= 7;
        }

        for (String method : methods) {
            text(sb, x + 5, curY, "/F1", 7, 0.15f, 0.2f, 0.25f, method);
            curY -= 10;
        }
    }

    private static void rect(StringBuilder sb, int x, int y, int w, int h, float r, float g, float b, boolean fill) {
        sb.append(String.format("%.3f %.3f %.3f %s\n", r, g, b, fill ? "rg" : "RG"));
        sb.append(String.format("%d %d %d %d re %s\n", x, y, w, h, fill ? "f" : "S"));
    }

    private static void line(StringBuilder sb, int x1, int y1, int x2, int y2, float r, float g, float b) {
        sb.append(String.format("%.3f %.3f %.3f RG\n", r, g, b));
        sb.append(String.format("%d %d m %d %d l S\n", x1, y1, x2, y2));
    }

    private static void text(StringBuilder sb, int x, int y, String font, int size, float r, float g, float b,
            String str) {
        sb.append("BT\n");
        sb.append(String.format("%s %d Tf\n", font, size));
        sb.append(String.format("%.3f %.3f %.3f rg\n", r, g, b));
        sb.append(String.format("%d %d Td\n", x, y));
        sb.append("(").append(escapePdf(str)).append(") Tj\n");
        sb.append("ET\n");
    }

    private static String escapePdf(String str) {
        return str.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
