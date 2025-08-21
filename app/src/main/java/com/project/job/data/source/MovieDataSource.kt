package com.project.job.data.source

//interface MovieDataSource {
//    /**
//     * Local
//     */
//    interface Local {
//        suspend fun getMoviesLocal(): List<Movie>
//        suspend fun updateMovies(movies: List<Movie>)
//        suspend fun getMovieDetailLocal(movieId: Int): Movie
//    }
//
//    /**
//     * Remote
//     */
//    interface Remote {
//        suspend fun getMovies(): BaseResponse<List<Movie>>
//
//        suspend fun getMovieDetail(movieId: Int): Movie
//    }
//}