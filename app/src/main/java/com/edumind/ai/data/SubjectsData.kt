package com.edumind.ai.data

/** A subject subcategory (a single learnable topic). */
data class SubjectItem(
    val id: String,
    val name: String,
)

/** One of the 16 top-level subject categories. */
data class SubjectCategory(
    val id: String,
    val name: String,
    val description: String,
    val iconKey: String,
    val subcategories: List<SubjectItem>,
)

/** Learning levels supported across the app. */
val LearningLevels = listOf(
    "School",
    "High School",
    "College",
    "Undergraduate",
    "Postgraduate",
    "Professional",
)

private fun items(categoryId: String, vararg names: String): List<SubjectItem> =
    names.mapIndexed { index, name ->
        SubjectItem(id = "$categoryId-$index", name = name)
    }

/** All 16 subject categories from the EduMind AI specification. */
val SubjectCategories: List<SubjectCategory> = listOf(
    SubjectCategory(
        id = "mathematics",
        name = "Mathematics",
        description = "Numbers, algebra, calculus, geometry and statistics.",
        iconKey = "math",
        subcategories = items(
            "mathematics",
            "Basic Mathematics", "Algebra", "Linear Algebra", "Calculus",
            "Differential Equations", "Geometry", "Trigonometry", "Number Theory",
            "Discrete Mathematics", "Probability", "Statistics",
            "Applied Mathematics", "Numerical Analysis",
        ),
    ),
    SubjectCategory(
        id = "english",
        name = "English and Languages",
        description = "Grammar, literature, writing and communication.",
        iconKey = "language",
        subcategories = items(
            "english",
            "English Grammar", "English Literature", "Academic Writing",
            "Creative Writing", "Vocabulary", "Reading Comprehension",
            "Essay Writing", "Linguistics", "Communication Skills",
            "Translation", "Other World Languages",
        ),
    ),
    SubjectCategory(
        id = "physics",
        name = "Physics",
        description = "Mechanics, energy, waves, quantum and astrophysics.",
        iconKey = "physics",
        subcategories = items(
            "physics",
            "Classical Mechanics", "Thermodynamics", "Electromagnetism",
            "Optics", "Quantum Physics", "Nuclear Physics", "Modern Physics",
            "Astrophysics", "Fluid Mechanics",
        ),
    ),
    SubjectCategory(
        id = "chemistry",
        name = "Chemistry",
        description = "Organic, inorganic, physical and biochemistry.",
        iconKey = "chemistry",
        subcategories = items(
            "chemistry",
            "Organic Chemistry", "Inorganic Chemistry", "Physical Chemistry",
            "Analytical Chemistry", "Biochemistry", "Environmental Chemistry",
            "Industrial Chemistry",
        ),
    ),
    SubjectCategory(
        id = "biology",
        name = "Biology",
        description = "Life sciences from cells to ecosystems.",
        iconKey = "biology",
        subcategories = items(
            "biology",
            "Cell Biology", "Genetics", "Microbiology", "Molecular Biology",
            "Human Anatomy", "Physiology", "Zoology", "Botany", "Ecology",
            "Biotechnology",
        ),
    ),
    SubjectCategory(
        id = "computer-science",
        name = "Computer Science and IT",
        description = "Programming, data, networks and software.",
        iconKey = "code",
        subcategories = items(
            "computer-science",
            "Computer Fundamentals", "Programming", "Python", "Java",
            "C and C++", "JavaScript", "HTML and CSS", "Data Structures",
            "Algorithms", "Database Management", "Networking", "Cybersecurity",
            "Cloud Computing", "Operating Systems", "Software Engineering",
            "Web Development", "Mobile App Development",
        ),
    ),
    SubjectCategory(
        id = "ai",
        name = "Artificial Intelligence",
        description = "Machine learning, deep learning and data science.",
        iconKey = "ai",
        subcategories = items(
            "ai",
            "Artificial Intelligence", "Machine Learning", "Deep Learning",
            "Neural Networks", "Natural Language Processing", "Computer Vision",
            "Generative AI", "Robotics", "Data Science",
        ),
    ),
    SubjectCategory(
        id = "engineering",
        name = "Engineering",
        description = "Electrical, mechanical, civil and more.",
        iconKey = "engineering",
        subcategories = items(
            "engineering",
            "Electrical Engineering", "Mechanical Engineering", "Civil Engineering",
            "Software Engineering", "Electronics Engineering", "Chemical Engineering",
            "Industrial Engineering", "Computer Engineering", "Aerospace Engineering",
            "Environmental Engineering",
        ),
    ),
    SubjectCategory(
        id = "business",
        name = "Business and Management",
        description = "Management, marketing, HR and entrepreneurship.",
        iconKey = "business",
        subcategories = items(
            "business",
            "Business Administration", "Marketing", "Human Resources",
            "Entrepreneurship", "Business Communication", "Strategic Management",
            "Project Management", "International Business", "Operations Management",
        ),
    ),
    SubjectCategory(
        id = "accounting",
        name = "Accounting and Finance",
        description = "Accounting, auditing, finance and banking.",
        iconKey = "finance",
        subcategories = items(
            "accounting",
            "Financial Accounting", "Cost Accounting", "Management Accounting",
            "Auditing", "Corporate Finance", "Investment Analysis", "Taxation",
            "Banking", "Financial Management",
        ),
    ),
    SubjectCategory(
        id = "economics",
        name = "Economics",
        description = "Micro, macro, development and econometrics.",
        iconKey = "economics",
        subcategories = items(
            "economics",
            "Microeconomics", "Macroeconomics", "Development Economics",
            "International Economics", "Econometrics", "Public Economics",
        ),
    ),
    SubjectCategory(
        id = "medical",
        name = "Medical and Health Sciences",
        description = "Anatomy, pharmacology, nursing and public health.",
        iconKey = "medical",
        subcategories = items(
            "medical",
            "Anatomy", "Physiology", "Pharmacology", "Pathology",
            "Microbiology", "Public Health", "Nursing", "Medical Terminology",
            "Biochemistry", "Health Sciences",
        ),
    ),
    SubjectCategory(
        id = "social-sciences",
        name = "Social Sciences",
        description = "Psychology, sociology, politics and philosophy.",
        iconKey = "social",
        subcategories = items(
            "social-sciences",
            "Psychology", "Sociology", "Political Science", "Anthropology",
            "International Relations", "Social Research", "Philosophy",
        ),
    ),
    SubjectCategory(
        id = "history-geography",
        name = "History and Geography",
        description = "World history, geography and archaeology.",
        iconKey = "history",
        subcategories = items(
            "history-geography",
            "World History", "Ancient History", "Modern History", "Geography",
            "Human Geography", "Physical Geography", "Environmental Studies",
            "Archaeology",
        ),
    ),
    SubjectCategory(
        id = "law",
        name = "Law and Legal Studies",
        description = "Constitutional, criminal, civil and international law.",
        iconKey = "law",
        subcategories = items(
            "law",
            "Constitutional Law", "Criminal Law", "Civil Law",
            "International Law", "Business Law", "Human Rights",
            "Legal Research",
        ),
    ),
    SubjectCategory(
        id = "other",
        name = "Other Academic Subjects",
        description = "Arts, media, agriculture, design and more.",
        iconKey = "other",
        subcategories = items(
            "other",
            "Agriculture", "Architecture", "Education", "Journalism",
            "Media Studies", "Communication", "Environmental Science",
            "Arts and Design", "Music Theory", "Religious Studies",
            "Sports Science", "Hospitality and Tourism",
        ),
    ),
)

fun findCategory(id: String): SubjectCategory? = SubjectCategories.find { it.id == id }

fun findSubject(subjectId: String): Pair<SubjectCategory, SubjectItem>? {
    for (category in SubjectCategories) {
        val item = category.subcategories.find { it.id == subjectId }
        if (item != null) return category to item
    }
    return null
}
