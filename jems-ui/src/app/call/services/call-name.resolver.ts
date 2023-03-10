import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from '@angular/router';
import {Observable, of} from 'rxjs';
import {CallStore} from './call-store.service';
import {map} from 'rxjs/operators';

@Injectable()
export class CallNameResolver implements Resolve<Observable<string>> {

  constructor(private callStore: CallStore) {
  }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Observable<string>> {
    return of(
      this.callStore.call$
        .pipe(
          map(call => call.name)
        )
    );
  }

}
