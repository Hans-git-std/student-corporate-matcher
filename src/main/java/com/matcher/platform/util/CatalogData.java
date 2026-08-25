package com.matcher.platform.util;

import java.util.*;

public final class CatalogData {

    public record DomainCatalog(
            String domainCode,
            String domainName,
            String description,
            List<String> subjects,
            List<String> skills
    ) {}

    private static final List<DomainCatalog> DOMAINS = List.of(
            new DomainCatalog(
                    "CSE",
                    "Computer Science & Information Technology",
                    "Software engineering, computer systems, algorithms, and cloud computing",
                    List.of(
                            "Data Structures & Algorithms",
                            "Operating Systems",
                            "Database Management Systems",
                            "Computer Networks",
                            "Theory of Computation",
                            "Compiler Design",
                            "Software Engineering",
                            "Object Oriented Programming",
                            "Computer Organization & Architecture",
                            "Cloud Computing",
                            "Web Technologies",
                            "Distributed Systems"
                    ),
                    List.of(
                            "Java", "Python", "C++", "Spring Boot", "Kafka", "Docker", "Kubernetes",
                            "Linux", "SQL", "PostgreSQL", "MongoDB", "React", "Node.js", "AWS",
                            "Git", "REST APIs", "Microservices", "Redis", "CI/CD"
                    )
            ),
            new DomainCatalog(
                    "MECH",
                    "Mechanical Engineering",
                    "Thermal systems, mechanics, manufacturing, machine design, and CAD/CAM",
                    List.of(
                            "Thermodynamics",
                            "Fluid Mechanics",
                            "Internal Combustion Engines",
                            "Theory of Machines",
                            "Heat and Mass Transfer",
                            "Manufacturing Technology",
                            "Strength of Materials",
                            "Design of Machine Elements",
                            "Dynamics of Machinery",
                            "CAD/CAM and Robotics",
                            "Automobile Engineering",
                            "Refrigeration & Air Conditioning"
                    ),
                    List.of(
                            "AutoCAD", "SolidWorks", "ANSYS", "CATIA", "CFD", "FEA", "MATLAB",
                            "CNC Programming", "GD&T", "Thermodynamic Modeling", "3D Printing",
                            "Hydraulics & Pneumatics", "Machining & Welding", "IC Engine Tuning"
                    )
            ),
            new DomainCatalog(
                    "ECE",
                    "Electronics & Communication Engineering",
                    "Embedded systems, VLSI design, signal processing, and telecommunications",
                    List.of(
                            "Digital Signal Processing",
                            "VLSI Design",
                            "Microprocessors & Microcontrollers",
                            "Analog Electronic Circuits",
                            "Digital Electronics",
                            "Signals and Systems",
                            "Electromagnetic Fields",
                            "Communication Systems",
                            "Embedded Systems",
                            "Wireless and Mobile Communication",
                            "Control Systems",
                            "Optical Communication"
                    ),
                    List.of(
                            "Verilog", "VHDL", "Embedded C", "MATLAB", "Simulink", "Keil uVision",
                            "Arduino", "Raspberry Pi", "PCB Design", "Altium Designer", "Cadence",
                            "FPGA Programming", "IoT Protocols", "RTOS", "Signal Analysis"
                    )
            ),
            new DomainCatalog(
                    "EE",
                    "Electrical Engineering",
                    "Power systems, electrical machines, power electronics, and industrial automation",
                    List.of(
                            "Power Systems",
                            "Electrical Machines",
                            "Power Electronics",
                            "Control Systems",
                            "Electrical Circuit Analysis",
                            "Renewable Energy Systems",
                            "Switchgear and Protection",
                            "High Voltage Engineering",
                            "Electric Drives & Control",
                            "Industrial Instrumentation"
                    ),
                    List.of(
                            "PLC SCADA", "MATLAB", "Simulink", "ETAP", "AutoCAD Electrical",
                            "Power System Analysis", "Motor Drives Control", "Solar PV Design",
                            "LabVIEW", "Relay Coordination", "Circuit Simulation"
                    )
            ),
            new DomainCatalog(
                    "CHEM",
                    "Chemical Engineering",
                    "Unit operations, chemical kinetics, process safety, and reaction engineering",
                    List.of(
                            "Chemical Reaction Engineering",
                            "Mass Transfer Operations",
                            "Heat Transfer Operations",
                            "Fluid Particle Mechanics",
                            "Process Dynamics & Control",
                            "Chemical Engineering Thermodynamics",
                            "Plant Design & Economics",
                            "Petroleum Refining & Petrochemicals",
                            "Process Equipment Design"
                    ),
                    List.of(
                            "Aspen Plus", "Aspen HYSYS", "MATLAB", "Process Flow Diagram (PFD)",
                            "P&ID Development", "Mass & Energy Balance", "Chemical Kinetics Modeling",
                            "Distillation Column Design", "HAZOP Analysis", "Safety Engineering"
                    )
            ),
            new DomainCatalog(
                    "CIVIL",
                    "Civil Engineering",
                    "Structural design, geotechnical analysis, transportation, and construction management",
                    List.of(
                            "Structural Analysis",
                            "Strength of Materials",
                            "Geotechnical Engineering",
                            "Design of Reinforced Concrete Structures",
                            "Transportation Engineering",
                            "Hydrology & Water Resources",
                            "Surveying & Geomatics",
                            "Environmental Engineering",
                            "Construction Planning & Management"
                    ),
                    List.of(
                            "AutoCAD Civil 3D", "Revit", "STAAD Pro", "ETABS", "GIS / ArcGIS",
                            "MS Project", "Primavera P6", "Structural Detailing", "Soil Mechanics Testing",
                            "Quantity Surveying", "Concrete Mix Design"
                    )
            ),
            new DomainCatalog(
                    "AI_DS",
                    "Artificial Intelligence & Data Science",
                    "Machine learning, neural networks, big data processing, and predictive analytics",
                    List.of(
                            "Machine Learning",
                            "Deep Learning",
                            "Artificial Intelligence",
                            "Natural Language Processing",
                            "Computer Vision",
                            "Big Data Analytics",
                            "Statistical Methods for Data Science",
                            "Data Mining & Warehousing",
                            "Reinforcement Learning"
                    ),
                    List.of(
                            "Python", "TensorFlow", "PyTorch", "Scikit-Learn", "Pandas", "NumPy",
                            "SQL", "Apache Spark", "Tableau", "PowerBI", "OpenCV", "Hugging Face",
                            "LLM Fine-Tuning", "Data Modeling", "MLOps"
                    )
            )
    );

    private CatalogData() {
    }

    public static List<DomainCatalog> getAllDomains() {
        return DOMAINS;
    }

    public static Optional<DomainCatalog> getDomainByCode(String code) {
        if (code == null) return Optional.empty();
        return DOMAINS.stream()
                .filter(d -> d.domainCode().equalsIgnoreCase(code.trim()))
                .findFirst();
    }

    public static Set<String> getAllStandardSubjects() {
        Set<String> subjects = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (DomainCatalog dc : DOMAINS) {
            subjects.addAll(dc.subjects());
        }
        return subjects;
    }

    public static Set<String> getAllStandardSkills() {
        Set<String> skills = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (DomainCatalog dc : DOMAINS) {
            skills.addAll(dc.skills());
        }
        return skills;
    }
}
