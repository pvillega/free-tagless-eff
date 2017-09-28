# Free Monad is dead, long live Free Monad!

As we all know, technology follows cycles. Not so long ago [Free Monad](http://timperrett.com/2013/11/21/free-monads-part-1/) was all the rage. Talks at Scala World seemed to indicate this is out of favour and instead you should use [Church encoding](https://underscore.io/blog/posts/2017/06/02/uniting-church-and-state.html) instead. In this brief post I want to explore a few competing approached to the same result, as discuss the pro and cons of each one.


<!-- more -->

free
http://www.parsonsmatt.org/2017/09/22/what_does_free_buy_us.html
https://markkarpov.com/post/free-monad-considered-harmful.html
http://degoes.net/articles/modern-fp-part-2

tagless 
https://www.beyondthelines.net/programming/introduction-to-tagless-final/
https://blog.scalac.io/exploring-tagless-final.html
https://oleksandrmanzyuk.wordpress.com/2014/06/18/from-object-algebras-to-finally-tagless-interpreters-2/


freer monad
http://okmij.org/ftp/Computation/free-monad.html 
https://github.com/atnos-org/eff
http://atnos-org.github.io/eff/org.atnos.site.CreateEffects.html

stack safety on free vs tagless
 

# Conclusion

As you would expect, there is no silver bullet. Each option has some benefits and some limitations.