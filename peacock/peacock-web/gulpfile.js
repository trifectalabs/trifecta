var del = require('del');
var gulp = require('gulp');
var elm  = require('gulp-elm');
var concat = require('gulp-concat');
var uglify = require('gulp-uglify');
var stylus = require('gulp-stylus');
var autoprefixer = require('gulp-autoprefixer');
var minifyCSS = require('gulp-minify-css');

var build = '../peacock-server/public/';

gulp.task('elm-init', elm.init);

gulp.task('elm', ['elm-init'], function(){
  return gulp.src('src/elm/Main.elm')
    .pipe(elm())
    //.pipe(uglify())
    .pipe(concat('app.min.js'))
    .pipe(gulp.dest(build + 'js'))
});

gulp.task('styles', function(){
  return gulp.src('src/styles/main.styl')
    .pipe(stylus())
    .pipe(minifyCSS())
    .pipe(autoprefixer({browsers: ['> 1%', 'last 2 versions']}))
    .pipe(concat('style.min.css'))
    .pipe(gulp.dest(build + 'css'))
});

gulp.task('fonts', function(){
  return gulp.src('src/fonts/*.{eot,svg,ttf,woff}')
    .pipe(gulp.dest(build + 'fonts'));
});

gulp.task('images', function(){
  return gulp.src('src/images/**/*.{png,jpg,jpeg,gif,svg}')
    .pipe(gulp.dest(build + 'img'));
});

gulp.task('external-js', function(){
  return gulp.src(
    ['node_modules/bootstrap-notify/bootstrap-notify.min.js',
     'node_modules/jquery/dist/jquery.min.js'])
    .pipe(gulp.dest(build + 'external/js'));
})

gulp.task('external-css', function(){
  return gulp.src(
    ['node_modules/animate.css/animate.min.css'])
    .pipe(gulp.dest(build + 'external/css'));
})

gulp.task('external', ['external-js', 'external-css']);

gulp.task('build', ['elm', 'styles', 'fonts', 'images', 'external']);

gulp.task('clean', function(){
  return del([build], {force: true});
});

gulp.task('watch', function() {
  gulp.watch('src/elm/**/*.elm', ['elm']);
  gulp.watch('src/styles/*.styl', ['styles']);
});

gulp.task('default', ['build', 'watch']);
