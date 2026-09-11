package com.ecosystem.util;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure Java PDF Generator for creating the official COMP2000 UML Design Diagram PDF.
 * Uses standard JRE only (zero external libraries).
 */
public class UMLDiagramPdfGenerator {

    public static void main(String[] args) {
        String outputPath = args.length > 0 ? args[0] : "Ecosystem_Design_Diagram.pdf";
        try {
            generateUmlPdf(outputPath);
            System.out.println("Successfully generated UML Design Diagram PDF at: " + outputPath);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void generateUmlPdf(String filePath) throws IOException {
        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        List<Long> offsets = new ArrayList<>();

        // 1. PDF Header
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

        // Build Content Stream (Drawing commands in PDF graphics instructions)
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
        write(pdfStream, "trailer\n<< /Size " + (offsets.size() + 1) + " /Root 1 0 R >>\nstartxref\n" + xrefStart + "\n%%EOF\n");

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

        // Header Title
        text(sb, 340, 805, "/F2", 20, 0.05f, 0.1f, 0.2f, "COMP2000: Object Oriented Programming Practices");
        text(sb, 375, 785, "/F1", 13, 0.35f, 0.45f, 0.55f, "System Architecture & UML Class Diagram - Multi-Tier Ecosystem");

        // Column 1: com.ecosystem.model (X: 30, W: 265)
        packageBox(sb, 30, 200, 265, 560, "PACKAGE com.ecosystem.model");
        drawUmlClass(sb, 40, 560, 245, 175, "<<abstract>>", "Entity", "#ede9fe",
                new String[]{"- id : long", "- position : Vector2D", "- alive : boolean", "- radius : double", "- color : Color"},
                new String[]{"+ update(grid, dt)* : void", "+ render(g)* : void", "+ markDead() : void", "# onDeath() : void"}
        );
        drawUmlClass(sb, 40, 310, 245, 230, "<<abstract>> extends Entity", "Organism", "#ede9fe",
                new String[]{"- energy, maxEnergy : double", "- age, maxAge : double", "- reproductionThreshold : double", "- genome : Genome", "- generation : int"},
                new String[]{"+ reproduce(grid)* : Organism", "+ canReproduce() : boolean", "+ burnBaseMetabolism(dt) : void", "+ consumeEnergy(amt) : void", "+ addEnergy(amt) : void"}
        );
        drawUmlClass(sb, 40, 215, 245, 80, "extends Entity", "Carcass", "#f1f5f9",
                new String[]{"- biomass, maxBiomass : double", "- decayTimer : double"},
                new String[]{"+ harvestBiomass(amt) : double", "+ update(grid, dt) : void"}
        );

        // Column 2: Entities & Genetics (X: 310, W: 265)
        packageBox(sb, 310, 200, 265, 560, "PACKAGE com.ecosystem.model.entities & genetics");
        drawUmlClass(sb, 320, 560, 245, 175, "<<abstract>> extends Organism", "Animal", "#ede9fe",
                new String[]{"- velocity : Vector2D", "- headingAngle : double", "- isSprinting : boolean"},
                new String[]{"# calculateSteeringForce(t, mult)", "# calculateFleeForce(threat, mult)", "# calculateWanderForce()", "# applyKineticMovement(grid, f, dt)"}
        );
        drawUmlClass(sb, 320, 420, 245, 125, "extends Animal", "Herbivore / Carnivore / Apex", "#f1f5f9",
                new String[]{"- visionRadius : double", "- packAlignment : double"},
                new String[]{"+ update(grid, dt) [Polymorphic AI]", "+ reproduce(grid) : Animal", "+ createDefault(pos) : Entity"}
        );
        drawUmlClass(sb, 320, 295, 245, 110, "<<generic>> <T>", "Chromosome<T>", "#ecfdf5",
                new String[]{"- traitName : String", "- allele : T"},
                new String[]{"+ mutateDouble(rng, rate, std, min, max)", "+ getAllele() : T", "+ setAllele(allele : T) : void"}
        );
        drawUmlClass(sb, 320, 215, 245, 68, "Encapsulation", "Genome", "#f1f5f9",
                new String[]{"- speed, vision, size, rate Genes"},
                new String[]{"+ validate() throws InvalidGenomeEx", "+ mutate() : Genome"}
        );

        // Column 3: Grid & Generics & Exceptions (X: 590, W: 275)
        packageBox(sb, 590, 200, 275, 560, "PACKAGE com.ecosystem.grid & exceptions");
        drawUmlClass(sb, 600, 555, 255, 180, "<<generic>> <T extends Entity>", "SpatialGrid<T>", "#ecfdf5",
                new String[]{"- worldWidth, worldHeight : double", "- cellSize : double", "- cells : Cell[][]", "- allEntities : List<T>"},
                new String[]{"+ addEntity(e : T) throws OutOfBoundsEx", "+ queryEntities<E>(cls : Class<E>) : List<E>", "+ queryEntitiesNear<E>(pos, r, cls)", "+ findNearest<E>(pos, r, cls, pred)"}
        );
        drawUmlClass(sb, 600, 410, 255, 130, "<<generic>> <T extends Number>", "BoundedResource<T>", "#ecfdf5",
                new String[]{"- name : String", "- maxCapacity, currentAmount : double"},
                new String[]{"+ replenish(amt : double) : void", "+ consume(amt) throws ResourceDepletedEx", "+ hasAvailable() : boolean", "+ getFillPercentage() : double"}
        );
        drawUmlClass(sb, 600, 280, 255, 115, "<<checked exception>>", "EcosystemException Hierarchy", "#fef2f2",
                new String[]{"extends Exception (Base Checked Exception)"},
                new String[]{"▲ EntityOutOfBoundsException", "▲ ResourceDepletedException", "▲ InvalidGenomeException", "▲ InvalidConfigurationException"}
        );
        drawUmlClass(sb, 600, 215, 255, 55, "Tile Container", "Cell", "#f1f5f9",
                new String[]{"- gridX, gridY : int | - biome : BiomeType"},
                new String[]{"+ extractNutrients(amt) throws ResDepleted"}
        );

        // Column 4: Core Engine & UI (X: 880, W: 280)
        packageBox(sb, 880, 200, 280, 560, "PACKAGE com.ecosystem.core & ui");
        drawUmlClass(sb, 890, 555, 260, 180, "Simulation Coordinator", "Engine", "#f1f5f9",
                new String[]{"- grid : SpatialGrid<Entity>", "- running : boolean", "- speedMultiplier : double", "- plant, herb, carn, apex History"},
                new String[]{"+ update(deltaSeconds : double) : void", "+ populateWorld() : void", "+ reset() : void", "+ recordStatistics() : void"}
        );
        drawUmlClass(sb, 890, 410, 260, 130, "extends JFrame", "SimulationFrame", "#f1f5f9",
                new String[]{"- simulationPanel : SimulationPanel", "- statisticsHUD : StatisticsHUD", "- controlPanel : ControlPanel", "- animationTimer : Timer (60 FPS)"},
                new String[]{"+ startSimulation() : void"}
        );
        drawUmlClass(sb, 890, 305, 260, 90, "Config Parser", "ConfigLoader", "#f1f5f9",
                new String[]{"Static Configuration Parser"},
                new String[]{"+ parseConfig(data : String)", "  throws InvalidConfigurationException"}
        );
        drawUmlClass(sb, 890, 215, 260, 75, "Custom Swing JPanel", "SimulationPanel / StatisticsHUD", "#f1f5f9",
                new String[]{"- paintComponent(Graphics g) : void"},
                new String[]{"+ drawPopulationGraph(Graphics2D g2)", "+ spawnEntityAt(Vector2D pos)"}
        );

        // Bottom Architectural Rationale Summary Box (X: 30, Y: 25, W: 1130, H: 160)
        rect(sb, 30, 25, 1130, 160, 1f, 1f, 1f, true);
        rect(sb, 30, 25, 1130, 160, 0.8f, 0.85f, 0.9f, false);
        text(sb, 45, 165, "/F2", 12, 0.05f, 0.1f, 0.2f, "COMP2000 Week 7 Rubric Mapping & Object-Oriented Principles");

        // 3 Summary Columns
        text(sb, 45, 145, "/F2", 10, 0.15f, 0.35f, 0.7f, "1. Inheritance & Polymorphism (20%)");
        text(sb, 45, 130, "/F1", 9, 0.25f, 0.3f, 0.35f, "* Hierarchy: Entity -> Organism -> Animal -> Herbivore, Carnivore, ApexPredator");
        text(sb, 45, 115, "/F1", 9, 0.25f, 0.3f, 0.35f, "* Polymorphic dynamic dispatch in Engine.update() calling entity.update() and entity.render()");
        text(sb, 45, 100, "/F1", 9, 0.25f, 0.3f, 0.35f, "* Subclasses override sensing, fleeing, grazing, hunting, and reproductive mutations");
        text(sb, 45, 85, "/F1", 9, 0.25f, 0.3f, 0.35f, "* Encapsulation preserves internal energy/velocity states with validated mutations");

        text(sb, 420, 145, "/F2", 10, 0.05f, 0.55f, 0.35f, "2. Generics & Type Safety (20%)");
        text(sb, 420, 130, "/F1", 9, 0.25f, 0.3f, 0.35f, "* SpatialGrid<T extends Entity> provides type-bounded 2D spatial partitioning");
        text(sb, 420, 115, "/F1", 9, 0.25f, 0.3f, 0.35f, "* Generic query methods <E extends T> List<E> queryEntitiesNear(...) eliminate type-casts");
        text(sb, 420, 100, "/F1", 9, 0.25f, 0.3f, 0.35f, "* Chromosome<T> generically encapsulates hereditary alleles and Gaussian mutations");
        text(sb, 420, 85, "/F1", 9, 0.25f, 0.3f, 0.35f, "* BoundedResource<T extends Number> guarantees mathematical bound safety");

        text(sb, 790, 145, "/F2", 10, 0.75f, 0.15f, 0.15f, "3. Exceptions & Architecture (20%)");
        text(sb, 790, 130, "/F1", 9, 0.25f, 0.3f, 0.35f, "* Custom checked hierarchy rooted at EcosystemException");
        text(sb, 790, 115, "/F1", 9, 0.25f, 0.3f, 0.35f, "* EntityOutOfBoundsException caught in Engine loop with recovery clamp");
        text(sb, 790, 100, "/F1", 9, 0.25f, 0.3f, 0.35f, "* InvalidConfigurationException & InvalidGenomeException protect state initialization");
        text(sb, 790, 85, "/F1", 9, 0.25f, 0.3f, 0.35f, "* Zero external dependencies - pure standard JRE Java 21 Swing & Java2D graphics");
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

        int headerH = stereotype.isEmpty() ? 22 : 30;
        float hr = headerBg.equals("#ede9fe") ? 0.93f : (headerBg.equals("#ecfdf5") ? 0.92f : (headerBg.equals("#fef2f2") ? 0.99f : 0.95f));
        float hg = headerBg.equals("#ede9fe") ? 0.91f : (headerBg.equals("#ecfdf5") ? 0.99f : (headerBg.equals("#fef2f2") ? 0.95f : 0.96f));
        float hb = headerBg.equals("#ede9fe") ? 0.99f : (headerBg.equals("#ecfdf5") ? 0.96f : (headerBg.equals("#fef2f2") ? 0.95f : 0.97f));

        rect(sb, x, y + h - headerH, w, headerH, hr, hg, hb, true);
        line(sb, x, y + h - headerH, x + w, y + h - headerH, 0.25f, 0.3f, 0.35f);

        if (!stereotype.isEmpty()) {
            text(sb, x + 8, y + h - 11, "/F3", 8, 0.45f, 0.5f, 0.55f, stereotype);
            text(sb, x + 8, y + h - 23, "/F2", 10, 0.05f, 0.1f, 0.15f, name);
        } else {
            text(sb, x + 8, y + h - 15, "/F2", 10, 0.05f, 0.1f, 0.15f, name);
        }

        int curY = y + h - headerH - 12;
        for (String field : fields) {
            text(sb, x + 6, curY, "/F1", 8, 0.2f, 0.25f, 0.3f, field);
            curY -= 11;
        }

        line(sb, x, curY + 4, x + w, curY + 4, 0.85f, 0.88f, 0.92f);
        curY -= 8;

        for (String method : methods) {
            text(sb, x + 6, curY, "/F1", 8, 0.15f, 0.2f, 0.25f, method);
            curY -= 11;
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

    private static void text(StringBuilder sb, int x, int y, String font, int size, float r, float g, float b, String str) {
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
