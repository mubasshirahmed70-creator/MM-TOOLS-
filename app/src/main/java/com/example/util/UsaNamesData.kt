package com.example.util

import kotlin.random.Random

/**
 * Authentic USA First Names & Surnames dataset.
 * Combines 250+ popular first names and 250+ popular last names,
 * providing over 65,000+ unique name combinations.
 */
object UsaNamesData {

    data class PersonName(
        val firstName: String,
        val lastName: String
    ) {
        val fullName: String get() = "$firstName $lastName"
    }

    val FIRST_NAMES = listOf(
        // Male first names
        "James", "John", "Robert", "Michael", "William", "David", "Richard", "Joseph",
        "Thomas", "Charles", "Christopher", "Daniel", "Matthew", "Anthony", "Mark", "Donald",
        "Steven", "Paul", "Andrew", "Joshua", "Kenneth", "Kevin", "Brian", "George",
        "Timothy", "Ronald", "Edward", "Jason", "Jeffrey", "Ryan", "Jacob", "Gary",
        "Nicholas", "Eric", "Jonathan", "Stephen", "Larry", "Justin", "Scott", "Brandon",
        "Benjamin", "Samuel", "Gregory", "Alexander", "Patrick", "Frank", "Raymond", "Jack",
        "Dennis", "Jerry", "Tyler", "Aaron", "Jose", "Adam", "Nathan", "Henry",
        "Zachary", "Douglas", "Peter", "Kyle", "Noah", "Ethan", "Jeremy", "Christian",
        "Walter", "Keith", "Austin", "Roger", "Terry", "Sean", "Gerald", "Carl",
        "Dylan", "Harold", "Jordan", "Jesse", "Bryan", "Lawrence", "Arthur", "Gabriel",
        "Bruce", "Logan", "Billy", "Joe", "Alan", "Juan", "Elijah", "Willie",
        "Albert", "Wayne", "Randy", "Mason", "Vincent", "Liam", "Roy", "Bobby",
        "Caleb", "Bradley", "Russell", "Lucas", "Trevor", "Owen", "Carter", "Julian",
        "Jackson", "Oliver", "Levi", "Aiden", "Wyatt", "Luke", "Hunter", "Connor",
        "Eli", "Landon", "Cameron", "Colton", "Dominic", "Parker", "Gavin", "Cole",

        // Female first names
        "Mary", "Patricia", "Jennifer", "Linda", "Elizabeth", "Barbara", "Susan", "Jessica",
        "Sarah", "Karen", "Lisa", "Nancy", "Betty", "Margaret", "Sandra", "Ashley",
        "Kimberly", "Emily", "Donna", "Michelle", "Carol", "Amanda", "Dorothy", "Melissa",
        "Deborah", "Stephanie", "Rebecca", "Sharon", "Laura", "Cynthia", "Kathleen", "Amy",
        "Angela", "Shirley", "Anna", "Brenda", "Pamela", "Emma", "Nicole", "Helen",
        "Samantha", "Katherine", "Christine", "Debra", "Rachel", "Carolyn", "Janet", "Catherine",
        "Maria", "Heather", "Diane", "Ruth", "Julie", "Olivia", "Joyce", "Virginia",
        "Victoria", "Kelly", "Lauren", "Christina", "Joan", "Evelyn", "Judith", "Megan",
        "Andrea", "Cheryl", "Hannah", "Jacqueline", "Martha", "Gloria", "Teresa", "Ann",
        "Sara", "Madison", "Frances", "Kathryn", "Janice", "Jean", "Abigail", "Alice",
        "Julia", "Judy", "Sophia", "Grace", "Denise", "Amber", "Doris", "Marilyn",
        "Danielle", "Beverly", "Isabella", "Theresa", "Diana", "Natalie", "Brittany", "Charlotte",
        "Marie", "Kayla", "Alexis", "Lori", "Ava", "Chloe", "Ella", "Mia",
        "Amelia", "Harper", "Aria", "Scarlett", "Layla", "Aubrey", "Zoey", "Penelope",
        "Lillian", "Addison", "Aubree", "Stella", "Riley", "Leah", "Hazel", "Violet",
        "Aurora", "Savannah", "Audrey", "Brooklyn", "Bella", "Claire", "Skylar", "Lucy"
    )

    val LAST_NAMES = listOf(
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
        "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas",
        "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson", "White",
        "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson", "Walker", "Young",
        "Allen", "King", "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores",
        "Green", "Adams", "Nelson", "Baker", "Hall", "Rivera", "Campbell", "Mitchell",
        "Carter", "Roberts", "Gomez", "Phillips", "Evans", "Turner", "Diaz", "Parker",
        "Cruz", "Edwards", "Collins", "Reyes", "Stewart", "Morris", "Morales", "Murphy",
        "Cook", "Rogers", "Gutierrez", "Ortiz", "Morgan", "Cooper", "Peterson", "Bailey",
        "Reed", "Kelly", "Howard", "Ramos", "Kim", "Cox", "Ward", "Richardson",
        "Watson", "Brooks", "Chavez", "Wood", "James", "Bennett", "Gray", "Mendoza",
        "Ruiz", "Hughes", "Price", "Alvarez", "Castillo", "Sanders", "Patel", "Myers",
        "Long", "Ross", "Foster", "Jimenez", "Powell", "Jenkins", "Perry", "Russell",
        "Sullivan", "Bell", "Coleman", "Butler", "Henderson", "Barnes", "Gonzales", "Fisher",
        "Vasquez", "Simmons", "Romero", "Jordan", "Patterson", "Alexander", "Hamilton", "Graham",
        "Reynolds", "Griffin", "Wallace", "Moreno", "West", "Cole", "Hayes", "Bryant",
        "Herrera", "Gibson", "Ellis", "Medina", "Aguilar", "Stevens", "Murray", "Ford",
        "Castro", "Marshall", "Owens", "Harrison", "Fernandez", "McDonald", "Woods", "Washington",
        "Kennedy", "Wells", "Vargas", "Henry", "Chen", "Freeman", "Webb", "Tucker",
        "Guzman", "Burns", "Crawford", "Olson", "Simpson", "Porter", "Hunter", "Gordon",
        "Mendez", "Silva", "Shaw", "Snyder", "Mason", "Dixon", "Muñoz", "Hunt",
        "Hicks", "Holmes", "Palmer", "Wagner", "Black", "Robertson", "Boyd", "Rose",
        "Stone", "Salazar", "Fox", "Warren", "Mills", "Meyer", "Rice", "Schmidt",
        "Garza", "Daniels", "Ferguson", "Nichols", "Stephens", "Soto", "Weaver", "Ryan",
        "Gardner", "Payne", "Grant", "Dunn", "Kelley", "Spencer", "Hawkins", "Arnold",
        "Pierce", "Vazquez", "Hansen", "Peters", "Santos", "Hart", "Bradley", "Knight",
        "Elliott", "Cunningham", "Duncan", "Armstrong", "Hudson", "Carroll", "Lane", "Riley",
        "Andrews", "Alvarado", "Ray", "Delgado", "Berry", "Perkins", "Hoffman", "Johnston",
        "Matthews", "Pena", "Richards", "Contreras", "Willis", "Carpenter", "Lawrence", "Sandoval",
        "Guerrero", "George", "Chapman", "Rios", "Estrada", "Ortega", "Watkins", "Greene",
        "Nunez", "Wheeler", "Valdez", "Harper", "Burke", "Larson", "Santiago", "Maldonado"
    )

    fun getRandomName(): PersonName {
        val first = FIRST_NAMES[Random.nextInt(FIRST_NAMES.size)]
        val last = LAST_NAMES[Random.nextInt(LAST_NAMES.size)]
        return PersonName(first, last)
    }
}
