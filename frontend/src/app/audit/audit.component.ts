
import {Component, OnDestroy, OnInit, QueryList, ViewChildren} from '@angular/core';
import {ApiService} from '../service/api.service';
import { MatPaginator, MatTableDataSource } from '@angular/material';
import {ConfigService} from "../service";
import {map} from "rxjs/operators";
import {MatSort} from "@angular/material/sort";



export class MessageAudit {
  user: string;
  recipients: string;
  success: boolean
  time: string;
  message: string;

  constructor(user: string, recipients: string, success: boolean, time: string, message: string) {
    this.user = user;
    this.recipients = recipients;
    this.success = success;
    this.time = time;
    this.message = message;
  }
}

export class Shift {
  user: string;
  name: string;
  tempid: string;
  client: string;
  orderid: string;
  confirmed: boolean;
  audit: string;
  time: string;

  constructor(user: string, name: string, tempid: string, client: string, orderid: string,
              confirmed: boolean, audit: string, time: string) {

    this.user = user;
    this.name = name;
    this.tempid = tempid;
    this.client = client;
    this.orderid = orderid;
    this.confirmed = confirmed;
    this.audit = audit;
    this.time = time;
  }
}




@Component({
  selector: 'app-dashboard',
  templateUrl: './audit.component.html',
  styleUrls: ['./audit.component.css']
})
export class AuditComponent implements OnInit, OnDestroy {

  constructor(
    private apiService: ApiService,
    private config: ConfigService,
  ) {

  }

  tempList: MessageAudit[] = [];
  shiftList: Shift[] = [];

  // User  Client  Shift  Location
  displayedColumns: string[] = ['name', 'user', 'tempid', 'client', 'orderid', 'confirmed', 'audit', 'time'];
  usertableColumns: string[] = ['user', 'recipients', 'success', 'time', 'message'];
  dataSource = new MatTableDataSource<Shift>(this.shiftList);
  usersDataSource = new MatTableDataSource<MessageAudit>(this.tempList);
  @ViewChildren(MatPaginator) paginator = new QueryList<MatPaginator>();
  @ViewChildren(MatSort) sort = new QueryList<MatSort>();

  ngOnInit() {
    this.dataSource.paginator = this.paginator.toArray()[0];
    this.usersDataSource.paginator = this.paginator.toArray()[1];
    this.updateShiftsTable();
    this.updateMessagesTable();
  }

  ngOnDestroy() {
  }

 updateShiftsTable() {

   this.shiftList = [];
   this.apiService.get(this.config.shifts_audit_url).pipe(
      map((arr) => arr.map(x => new Shift(x.username, x.fullName, x.tempid, x.clientName, x.orderid,
        x.confirmed, x.audit, x.time))))
      .subscribe(lists => {
        lists.forEach(shift => {
          this.shiftList.push(shift);
        });
        this.shiftList.reverse();
        this.dataSource = new MatTableDataSource(this.shiftList);
        this.dataSource.paginator = this.paginator.toArray()[0];
      });
  }

  updateMessagesTable() {

    this.tempList = [];
    this.apiService.get(this.config.messages_url).pipe(
      map((arr) => arr.map(x => new MessageAudit(x.username,  x.recipients, x.success, x.time, x.message)))).subscribe(lists => {
      lists.forEach(temp => {
        // console.log(temp.firstname);
        this.tempList.push(temp);
      });
      this.tempList.reverse();
      this.usersDataSource = new MatTableDataSource(this.tempList);
      this.usersDataSource.paginator = this.paginator.toArray()[1];
    });
  }

  applyUserFilter(filterValue: string) {
    this.usersDataSource.filter = filterValue.trim().toLowerCase();
  }

  applyShiftFilter(filterValue: string) {
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }


}

